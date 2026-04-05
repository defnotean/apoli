#!/usr/bin/env python3
"""
Exhaustive Mixin Audit for Apoli against MC 26.1 deobf jar.

Checks:
1. All method= targets in @Inject/@ModifyVariable/@ModifyReturnValue/@ModifyExpressionValue/
   @WrapMethod/@WrapOperation/@Redirect/@ModifyArg/@WrapWithCondition
2. All @Shadow method/field declarations
3. All @Accessor/@Invoker targets
4. All target= strings in @At annotations for wrong package paths
"""

import os
import re
import subprocess
import zipfile
import sys
import json
from pathlib import Path
from collections import defaultdict

MIXIN_DIR = r"C:\Users\Eating\Desktop\Minecraft\apoli\src\main\java\io\github\apace100\apoli\mixin"
MC_JAR = r"C:\Users\Eating\.gradle\caches\fabric-loom\minecraftMaven\net\minecraft\minecraft-merged-deobf\26.1\minecraft-merged-deobf-26.1.jar"
EXTRACT_DIR = r"C:\Users\Eating\AppData\Local\Temp\mc26"
JAVAP = r"C:\Program Files\Java\jdk-25\bin\javap"

# Cache for javap output per class
class_cache = {}
jar_entries = set()

def load_jar_entries():
    """Load all class entries from the MC jar"""
    global jar_entries
    with zipfile.ZipFile(MC_JAR) as z:
        jar_entries = set(z.namelist())

def extract_class(class_path):
    """Extract a class file from the MC jar"""
    with zipfile.ZipFile(MC_JAR) as z:
        if class_path in jar_entries:
            z.extract(class_path, EXTRACT_DIR)
            return True
        # Try inner classes
        return False

def get_class_methods_and_fields(fqcn):
    """
    Given a fully qualified class name (dot-separated), extract and javap it.
    Returns (set_of_method_names, set_of_field_names, raw_output)
    """
    if fqcn in class_cache:
        return class_cache[fqcn]

    # Convert dots to slashes for jar path
    class_path = fqcn.replace('.', '/') + '.class'

    if not extract_class(class_path):
        # Class doesn't exist in jar
        class_cache[fqcn] = (None, None, None)
        return (None, None, None)

    class_file = os.path.join(EXTRACT_DIR, class_path)
    try:
        result = subprocess.run(
            [JAVAP, '-p', class_file],
            capture_output=True, text=True, timeout=30
        )
        output = result.stdout
    except Exception as e:
        class_cache[fqcn] = (None, None, f"javap error: {e}")
        return (None, None, f"javap error: {e}")

    methods = set()
    fields = set()

    for line in output.split('\n'):
        line = line.strip()
        if not line or line.startswith('Compiled from') or line.startswith('public class') or \
           line.startswith('public abstract class') or line.startswith('public interface') or \
           line.startswith('public final class') or line.startswith('}') or \
           line.startswith('class ') or line.startswith('abstract class') or \
           line.startswith('final class') or line.startswith('sealed class'):
            continue

        # Method: has parentheses
        if '(' in line and ')' in line:
            # Extract method name - it's the word before the first (
            # Pattern: [modifiers] [return_type] methodName(params);
            paren_idx = line.index('(')
            before_paren = line[:paren_idx].strip()
            parts = before_paren.split()
            if parts:
                method_name = parts[-1]
                # Handle generic return types
                if '<' in method_name:
                    method_name = method_name.split('>')[-1]
                    if not method_name:
                        # The method name might be after >
                        continue
                methods.add(method_name)
        elif line.endswith(';') and '(' not in line:
            # Field declaration
            parts = line.rstrip(';').strip().split()
            if parts:
                field_name = parts[-1]
                fields.add(field_name)

    class_cache[fqcn] = (methods, fields, output)
    return (methods, fields, output)

def resolve_class_from_import(class_simple_name, imports, package):
    """Resolve a simple class name to FQCN using imports"""
    # Handle inner classes with dot notation like BlockBehaviour.BlockStateBase
    parts = class_simple_name.split('.')
    outer = parts[0]

    for imp in imports:
        if imp.endswith('.' + outer):
            if len(parts) > 1:
                return imp + '$' + '$'.join(parts[1:])
            return imp
        if imp.endswith('.*'):
            # Wildcard import - can't resolve definitively
            pass

    # Could be in same package
    return None

def parse_mixin_file(filepath):
    """
    Parse a mixin Java file and extract:
    - Target class (from @Mixin annotation)
    - All method= targets from injection annotations
    - All @Shadow declarations
    - All @Accessor/@Invoker targets
    - All target= strings from @At annotations
    """
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    lines = content.split('\n')
    results = {
        'file': filepath,
        'target_class': None,
        'target_class_fqcn': None,
        'method_targets': [],      # (line_no, annotation_type, method_string)
        'shadow_declarations': [], # (line_no, name, is_method)
        'accessor_targets': [],    # (line_no, annotation_type, name)
        'at_targets': [],          # (line_no, target_string)
        'imports': [],
    }

    # Extract imports
    for line in lines:
        line_stripped = line.strip()
        if line_stripped.startswith('import '):
            imp = line_stripped.replace('import ', '').replace(';', '').replace('static ', '').strip()
            results['imports'].append(imp)

    # Extract @Mixin target class
    # Pattern 1: @Mixin(ClassName.class)
    mixin_match = re.search(r'@Mixin\(\s*(\w+(?:\.\w+)*)\s*\.class\s*\)', content)
    if mixin_match:
        simple_name = mixin_match.group(1)
        fqcn = resolve_class_from_import(simple_name, results['imports'], None)
        results['target_class'] = simple_name
        results['target_class_fqcn'] = fqcn

    # Pattern 2: @Mixin(targets = "...")
    targets_match = re.search(r'@Mixin\(\s*targets\s*=\s*"([^"]+)"', content)
    if targets_match:
        target_str = targets_match.group(1)
        # Could be dot-separated or slash-separated
        fqcn = target_str.replace('/', '.')
        results['target_class'] = target_str
        results['target_class_fqcn'] = fqcn

    # Pattern 3: @Mixin(value = ClassName.class)
    if not results['target_class_fqcn']:
        mixin_val_match = re.search(r'@Mixin\(\s*value\s*=\s*(\w+(?:\.\w+)*)\s*\.class', content)
        if mixin_val_match:
            simple_name = mixin_val_match.group(1)
            fqcn = resolve_class_from_import(simple_name, results['imports'], None)
            results['target_class'] = simple_name
            results['target_class_fqcn'] = fqcn

    # Extract method= targets from injection annotations
    # Skip commented lines
    injection_annotations = [
        'Inject', 'ModifyVariable', 'ModifyReturnValue', 'ModifyExpressionValue',
        'WrapMethod', 'WrapOperation', 'Redirect', 'ModifyArg', 'WrapWithCondition',
        'ModifyArgs'
    ]

    # We need to track multi-line annotations
    # Strategy: find annotation blocks and extract method= from them
    in_comment = False
    i = 0
    while i < len(lines):
        line = lines[i]
        stripped = line.strip()

        # Skip line comments
        if stripped.startswith('//'):
            i += 1
            continue

        # Track block comments
        if '/*' in stripped:
            in_comment = True
        if '*/' in stripped:
            in_comment = False
            i += 1
            continue
        if in_comment:
            i += 1
            continue

        # Check for injection annotations
        for ann in injection_annotations:
            if f'@{ann}' in stripped:
                # Collect the full annotation (may span multiple lines)
                ann_text = stripped
                line_no = i + 1  # 1-based
                depth = ann_text.count('(') - ann_text.count(')')
                j = i + 1
                while depth > 0 and j < len(lines):
                    next_line = lines[j].strip()
                    if not next_line.startswith('//'):
                        ann_text += ' ' + next_line
                        depth += next_line.count('(') - next_line.count(')')
                    j += 1

                # Extract method= from annotation
                method_matches = re.findall(r'method\s*=\s*"([^"]+)"', ann_text)
                for m in method_matches:
                    results['method_targets'].append((line_no, ann, m))

                # Also extract method= array form: method = {"m1", "m2"}
                method_array = re.search(r'method\s*=\s*\{([^}]+)\}', ann_text)
                if method_array:
                    methods_str = method_array.group(1)
                    for m in re.findall(r'"([^"]+)"', methods_str):
                        results['method_targets'].append((line_no, ann, m))

                break

        # Check for @Shadow declarations
        if '@Shadow' in stripped and not stripped.startswith('//'):
            # Collect lines until we hit the declaration
            shadow_block = stripped
            j = i + 1
            while j < len(lines) and not shadow_block.rstrip().endswith(';') and '{' not in shadow_block:
                next_line = lines[j].strip()
                if next_line and not next_line.startswith('//'):
                    shadow_block += ' ' + next_line
                j += 1
                if shadow_block.rstrip().endswith(';') or '{' in shadow_block:
                    break

            # Determine if it's a method or field
            # Remove annotations
            decl = re.sub(r'@\w+(\([^)]*\))?\s*', '', shadow_block).strip()
            if '(' in decl:
                # Method - extract name before (
                paren_idx = decl.index('(')
                before_paren = decl[:paren_idx].strip()
                parts = before_paren.split()
                if parts:
                    name = parts[-1]
                    results['shadow_declarations'].append((i+1, name, True))
            elif decl.endswith(';'):
                parts = decl.rstrip(';').strip().split()
                if parts:
                    # Handle assignments like "field = value"
                    name = parts[-1].split('=')[0].strip()
                    results['shadow_declarations'].append((i+1, name, False))

        # Check for @Accessor
        if '@Accessor' in stripped and not stripped.startswith('//'):
            accessor_block = stripped
            j = i + 1
            while j < len(lines) and not accessor_block.rstrip().endswith(';') and \
                  not accessor_block.rstrip().endswith('}') and not accessor_block.rstrip().endswith('{'):
                next_line = lines[j].strip()
                if next_line and not next_line.startswith('//'):
                    accessor_block += ' ' + next_line
                j += 1
                if accessor_block.rstrip().endswith(';') or accessor_block.rstrip().endswith('}'):
                    break

            # Extract the target name from @Accessor("name") or infer from method name
            acc_target = re.search(r'@Accessor\(\s*"([^"]+)"\s*\)', accessor_block)
            if acc_target:
                results['accessor_targets'].append((i+1, 'Accessor', acc_target.group(1)))
            else:
                # Infer from method name - getXxx/setXxx/isXxx -> xxx (with lowercase first)
                method_match = re.search(r'\b(?:get|set|is)(\w+)\s*\(', accessor_block)
                if method_match:
                    field_name = method_match.group(1)
                    if field_name:
                        # Don't lowercase - accessor convention uses exact field name after get/set
                        results['accessor_targets'].append((i+1, 'Accessor', field_name))

        # Check for @Invoker
        if '@Invoker' in stripped and not stripped.startswith('//'):
            invoker_block = stripped
            j = i + 1
            while j < len(lines) and not invoker_block.rstrip().endswith(';') and \
                  not invoker_block.rstrip().endswith('}'):
                next_line = lines[j].strip()
                if next_line and not next_line.startswith('//'):
                    invoker_block += ' ' + next_line
                j += 1
                if invoker_block.rstrip().endswith(';') or invoker_block.rstrip().endswith('}'):
                    break

            inv_target = re.search(r'@Invoker\(\s*"([^"]+)"\s*\)', invoker_block)
            if inv_target:
                results['accessor_targets'].append((i+1, 'Invoker', inv_target.group(1)))

        # Check for @At target strings
        if '@At(' in stripped and not stripped.startswith('//'):
            at_block = stripped
            j = i + 1
            depth_at = at_block.count('(') - at_block.count(')')
            while depth_at > 0 and j < len(lines):
                next_line = lines[j].strip()
                if not next_line.startswith('//'):
                    at_block += ' ' + next_line
                    depth_at += next_line.count('(') - next_line.count(')')
                j += 1

            # Extract target= strings
            target_matches = re.findall(r'target\s*=\s*"([^"]+)"', at_block)
            for t in target_matches:
                results['at_targets'].append((i+1, t))

        i += 1

    return results

def strip_descriptor(method_str):
    """Strip method descriptor to get just the name. e.g. 'tick()V' -> 'tick'"""
    # Handle Lcom/foo/Bar;methodName(...)... format
    if method_str.startswith('L') and ';' in method_str:
        # This is a full method reference like Lnet/minecraft/...; - skip
        return None

    paren_idx = method_str.find('(')
    if paren_idx >= 0:
        return method_str[:paren_idx]
    return method_str

def check_at_target_class_path(target_str):
    """
    Check @At target strings for wrong package paths.
    target format: Lnet/minecraft/package/Class;methodName(Ldesc;)V
    Returns (class_path, exists_in_jar) or None
    """
    # Extract class reference from target
    class_matches = re.findall(r'L([^;]+);', target_str)
    issues = []
    for cls_path in class_matches:
        class_file = cls_path + '.class'
        if class_file not in jar_entries:
            issues.append(cls_path)
    return issues

def main():
    print("=" * 80)
    print("EXHAUSTIVE APOLI MIXIN AUDIT AGAINST MC 26.1")
    print("=" * 80)
    print()

    load_jar_entries()
    print(f"Loaded {len(jar_entries)} entries from MC jar")

    # Find all mixin files
    mixin_files = []
    for root, dirs, files in os.walk(MIXIN_DIR):
        for f in files:
            if f.endswith('.java') and f != 'package-info.java':
                mixin_files.append(os.path.join(root, f))

    print(f"Found {len(mixin_files)} mixin files to audit")
    print()

    all_issues = []
    files_checked = 0

    for filepath in sorted(mixin_files):
        rel_path = os.path.relpath(filepath, MIXIN_DIR)
        parsed = parse_mixin_file(filepath)

        if not parsed['target_class_fqcn']:
            # No target class found - might be a non-mixin file
            continue

        files_checked += 1
        fqcn = parsed['target_class_fqcn']

        # Get methods and fields for target class
        methods, fields, raw = get_class_methods_and_fields(fqcn)

        if methods is None:
            all_issues.append({
                'file': rel_path,
                'type': 'CLASS_NOT_FOUND',
                'target_class': fqcn,
                'details': f"Target class {fqcn} not found in MC 26.1 jar"
            })
            continue

        # Check method targets
        for line_no, ann_type, method_str in parsed['method_targets']:
            method_name = strip_descriptor(method_str)
            if method_name is None:
                continue
            if method_name == '<init>' or method_name == '<clinit>':
                continue  # Constructors always exist
            if method_name.startswith('lambda$'):
                continue  # Lambdas are compiler-generated

            if method_name not in methods:
                all_issues.append({
                    'file': rel_path,
                    'type': 'METHOD_NOT_FOUND',
                    'line': line_no,
                    'annotation': ann_type,
                    'method': method_str,
                    'method_name': method_name,
                    'target_class': fqcn,
                    'available_methods': sorted(methods) if len(methods) < 200 else '[too many to list]'
                })

        # Check @Shadow declarations
        for line_no, name, is_method in parsed['shadow_declarations']:
            if is_method:
                if name not in methods:
                    all_issues.append({
                        'file': rel_path,
                        'type': 'SHADOW_METHOD_NOT_FOUND',
                        'line': line_no,
                        'name': name,
                        'target_class': fqcn,
                    })
            else:
                if name not in fields:
                    all_issues.append({
                        'file': rel_path,
                        'type': 'SHADOW_FIELD_NOT_FOUND',
                        'line': line_no,
                        'name': name,
                        'target_class': fqcn,
                    })

        # Check @Accessor targets
        for line_no, ann_type, name in parsed['accessor_targets']:
            if ann_type == 'Accessor':
                # Should be a field OR a getter/setter method
                if name not in fields and name not in methods:
                    all_issues.append({
                        'file': rel_path,
                        'type': 'ACCESSOR_TARGET_NOT_FOUND',
                        'line': line_no,
                        'name': name,
                        'target_class': fqcn,
                    })
            elif ann_type == 'Invoker':
                if name not in methods:
                    all_issues.append({
                        'file': rel_path,
                        'type': 'INVOKER_TARGET_NOT_FOUND',
                        'line': line_no,
                        'name': name,
                        'target_class': fqcn,
                    })

        # Check @At target strings for wrong package paths
        for line_no, target_str in parsed['at_targets']:
            bad_paths = check_at_target_class_path(target_str)
            for bad_path in bad_paths:
                all_issues.append({
                    'file': rel_path,
                    'type': 'AT_TARGET_CLASS_NOT_FOUND',
                    'line': line_no,
                    'target_string': target_str,
                    'missing_class': bad_path,
                    'target_class': fqcn,
                })

    # Print results
    print("=" * 80)
    print(f"AUDIT COMPLETE: Checked {files_checked} mixin files")
    print(f"ISSUES FOUND: {len(all_issues)}")
    print("=" * 80)
    print()

    if all_issues:
        # Group by type
        by_type = defaultdict(list)
        for issue in all_issues:
            by_type[issue['type']].append(issue)

        for issue_type, issues in sorted(by_type.items()):
            print(f"\n{'='*60}")
            print(f"  {issue_type} ({len(issues)} issues)")
            print(f"{'='*60}")
            for issue in issues:
                print(f"\n  File: {issue['file']}")
                if 'line' in issue:
                    print(f"  Line: {issue['line']}")
                if 'annotation' in issue:
                    print(f"  Annotation: @{issue['annotation']}")
                if 'method' in issue:
                    print(f"  Method target: {issue['method']}")
                if 'method_name' in issue:
                    print(f"  Method name: {issue['method_name']}")
                if 'name' in issue:
                    print(f"  Name: {issue['name']}")
                if 'target_string' in issue:
                    print(f"  Target string: {issue['target_string']}")
                if 'missing_class' in issue:
                    print(f"  Missing class: {issue['missing_class']}")
                print(f"  Target class: {issue['target_class']}")

    # Also output as JSON for programmatic use
    with open(os.path.join(os.path.dirname(MIXIN_DIR), 'audit_results.json'), 'w') as f:
        json.dump(all_issues, f, indent=2, default=str)
    print(f"\nFull results written to audit_results.json")

if __name__ == '__main__':
    main()
