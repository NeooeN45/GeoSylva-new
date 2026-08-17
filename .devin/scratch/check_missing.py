import re
en = set(re.findall(r'name="([^"]+)"', open('app/src/main/res/values/strings.xml', encoding='utf-8').read()))
fr = set(re.findall(r'name="([^"]+)"', open('app/src/main/res/values-fr/strings.xml', encoding='utf-8').read()))
missing = sorted(en - fr)
print(f'Missing in FR: {len(missing)}')
for m in missing[:50]:
    print(f'  {m}')
