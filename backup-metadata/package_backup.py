from pathlib import Path
import json,hashlib,zipfile,time,shutil
W=Path(__file__).resolve().parent;OUT=W/'archives';OUT.mkdir(exist_ok=True)
items=json.loads((W/'inventory.json').read_text());excluded=json.loads((W/'exclusions.json').read_text())
H=lambda p:hashlib.file_digest(open(p,'rb'),'sha256').hexdigest()
plans={'KelMagic_FULL_BACKUP.zip':dict(items),'KelMagic_TRANSFER.zip':{k:v for k,v in items.items() if v['transfer']},'KelMagic_EXPERIMENTS_F2.zip':{k:v for k,v in items.items() if k.startswith(('experiments/','approved/','checkpoints/','references/')) or k in ['history/source-archives/KelMagic-E-PNG-mouth-nose-TEST.zip','history/source-archives/KelMagic-E-cheeks-controller-TEST.zip']}}
counts={n:{'payloadFiles':len(it),'payloadBytes':sum(v['size'] for v in it.values())} for n,it in plans.items()}
manifest='''# KelMagic — MANIFEST резервного переноса

## Назначение и состав
- KelMagic_FULL_BACKUP.zip: полный безопасный снимок всех найденных проектов/исходников и доступных исторических архивов, выводов сборок и старых QA-миров; это не образ всей ОС.
- KelMagic_TRANSFER.zip: исходный перенос без project .gradle, build, run и вложенных исторических архивов. Сохранены основные и экспериментальные исходники, libs, wrapper, ресурсы/UV/.bbmodel, генераторы, тесты, QA-code/init scripts, документация, доступные references и F2. Большие прежние ZIP и QA-миры остаются в FULL; см. historical-archive-index.json.
- KelMagic_EXPERIMENTS_F2.zip: независимый страхующий комплект D-final, E, F2 и ранних сравнительных геометрий/референсов; содержит полный исходный экспериментальный проект, не только модели, а также два исходных контрольных ZIP E.

Все три содержат корневые HANDOFF.md, MANIFEST.md, RECOVERY.md и metadata с хэшами/исключениями. Главный проект main/KelMagic не заменён экспериментом. Содержимое F2 находится вне assets и НЕ подключено.

## Исключения и безопасность
- Из обоих основных архивов исключены воспроизводимые .gradle/__pycache__/node_modules/.cache, JDK/JDK tarball, vanilla cache и decompiler download, временные PID/lock-файлы. Глобальная домашняя директория/браузер/credential store не захватывались.
- Учетные launcher/usercache/server-list/credentials/.env/private-key файлы исключаются по имени. Выполнен эвристический поиск секретоподобных значений в текстах и вложенных ZIP; обнаружений нет. Это не обещание невозможности любых неизвестных секретов; точный алгоритм и результат сохранены.
- Скрытые файлы не исключались массово: .github/.vscode/.gitignore/.gitattributes сохранены, если присутствовали.
- TRANSFER дополнительно исключает build/run и вложенные архивы. Последний E QA-мир отсутствовал уже в исходном контрольном ZIP. В FULL сохранены найденные другие старые QA-миры, не подмена последнего.
- Не включён весь .agent-service: перенесены только явно выбранные контрольные файлы/пользовательский screenshot, остальные локальные данные не требуются проекту.
- Полный пофайловый журнал исключений: backup-metadata/exclusions.json. Нулевой результат эвристики: backup-metadata/safety-review.json.

## Происхождение и оговорки полноты
E побайтно восстановлена из сохранённого контрольного ZIP, manifest 10546 файлов сверён. D-final из её frozen reference; ранний D хранится отдельно.
Исходный каталог F2 прошлого окружения недоступен. 19 спрайтов и два контактных листа восстановлены по сохранённым командам рисования/8× nearest-neighbor. Нынешние хэши доступны; сравнить с утраченными прежними байтами невозможно. Это восстановление существующего рисунка, не интеграция/новая версия дизайна. Подробности HANDOFF.md и RECOVERY.md.
Не все inline-картинки диалога доступны как файлы; сохраняется найденный набор concepts/refs/game evidence, без заявления о полноте всех первоначальных таргетов.
Git object database не обнаружена ни в каталогах, ни в сохранённых ZIP: bundle не создан. Доступна история снимков, не история коммитов.

## Контрольные суммы
Полный индекс имён/размеров/SHA256 исходных payload-файлов: backup-metadata/inventory.json. Ключевые source/model/texture/генераторы: backup-metadata/protected-files.json. Исторические ZIP: backup-metadata/historical-archive-index.json.
Каждый архив дополнительно содержит PAYLOAD_SHA256.json — имена, размеры и SHA256 всех включённых файлов, кроме самого этого индекса. HANDOFF.md и MANIFEST.md входят в индекс.
SHA256 самих архивов рассчитываются ПОСЛЕ закрытия ZIP и хранятся в отдельном ARCHIVE_CHECKSUMS.json, опубликованном рядом. Внутрь архива нельзя поместить его собственный конечный SHA256 без циклического изменения содержимого. Внешняя итоговая копия этого MANIFEST дополнена фактическими размерами/хэшами/проверками; внутренняя описывает состав и исходные контрольные суммы до запечатывания.

## Количество файлов до добавления общих документов/metadata
'''
for n,c in counts.items():manifest+=f"- {n}: {c['payloadFiles']} файлов, {c['payloadBytes']} исходных байт.\n"
manifest+='\n## Важные неизменённые артефакты (SHA256)\n'
keys=['main/KelMagic/build.gradle','approved/D-final-lashes/red_mage.geo.json','approved/D-final-lashes/red_mage_gecko.bbmodel','approved/D-final-lashes/red_mage_geo.png','experiments/E/test-project/src/main/resources/assets/kelmagic/geo/red_mage.geo.json','experiments/E/test-project/src/main/resources/assets/kelmagic/textures/entity/red_mage_geo.png','experiments/E/test-project/art/red-mage/red_mage_gecko.bbmodel','experiments/kelmagic-F2-art/art-F2/preview/F2-emotions-contact-8x.png','experiments/kelmagic-F2-art/art-F2/preview/F2-visemes-contact-8x.png']
for k in keys:
 if k in items:manifest+=f"- `{k}`: `{items[k]['sha256']}`\n"
(W/'MANIFEST.md').write_text(manifest)
metadata=['inventory.json','protected-files.json','historical-archive-index.json','exclusions.json','safety-review.json','E-archive-validation.json','git-status.json']
common={}
for name in ['HANDOFF.md','MANIFEST.md','RECOVERY.md']:
 p=W/name;common[name]={'source':str(p),'size':p.stat().st_size,'sha256':H(p)}
for name in metadata+['inventory_backup.py','package_backup.py']:
 p=W/name;common['backup-metadata/'+name]={'source':str(p),'size':p.stat().st_size,'sha256':H(p)}
checks={};validation={}
for name,plan in plans.items():
 plan.update(common);start=time.time();path=OUT/name
 listing={k:{'size':v['size'],'sha256':v['sha256']} for k,v in sorted(plan.items())}
 index=json.dumps(listing,indent=2).encode();print('PACKAGING',name,len(plan),'files',flush=True)
 with zipfile.ZipFile(path,'w',zipfile.ZIP_DEFLATED,compresslevel=4,allowZip64=True) as z:
  for k,v in sorted(plan.items()):z.write(v['source'],k)
  z.writestr('PAYLOAD_SHA256.json',index)
 print('VERIFYING',name,path.stat().st_size,'bytes',flush=True)
 readCount=0
 with zipfile.ZipFile(path) as z:
  names=z.namelist();assert len(names)==len(set(names));assert {'HANDOFF.md','MANIFEST.md','PAYLOAD_SHA256.json'}<=set(names)
  for k,v in listing.items():
   zi=z.getinfo(k);assert zi.file_size==v['size'],k
   # Reading every entry to EOF triggers zipfile CRC validation as well as our SHA256 comparison.
   with z.open(k) as f:sha=hashlib.file_digest(f,'sha256').hexdigest()
   assert sha==v['sha256'],(name,k);readCount+=1
  assert z.read('PAYLOAD_SHA256.json')==index
 sha=H(path);checks[name]={'bytes':path.stat().st_size,'sha256':sha,'entries':len(plan)+1,'payloadSHA256EntriesVerified':readCount}
 validation[name]={'zipEntriesReadToEOF_CRCchecked':readCount+1,'allPayloadSHA256Match':True,'noDuplicateNames':True,'handoffAndManifestPresent':True,'elapsedSeconds':round(time.time()-start,2)}
 print('VERIFIED',name,checks[name],flush=True)
(OUT/'ARCHIVE_CHECKSUMS.json').write_text(json.dumps(checks,indent=2));(W/'archive-validation.json').write_text(json.dumps(validation,indent=2));shutil.copy2(W/'archive-validation.json',OUT/'archive-validation.json')
# A sealed archive's own hash cannot be embedded in itself. Publish final verification as sidecars.
manifest+='\n## Фактические итоговые архивы (проверка выполнена после запечатывания)\n'
for n,c in checks.items():manifest+=f"\n### {n}\n- Размер: {c['bytes']} байт.\n- SHA256: `{c['sha256']}`.\n- Записей: {c['entries']}.\n- Прочитаны все записи с CRC-проверкой; сверены SHA256 всех {c['payloadSHA256EntriesVerified']} payload-файлов. HANDOFF и MANIFEST присутствуют.\n"
manifest+='\nНовых сборок и игровых тестов при резервировании не выполнялось. Проверка ZIP/SHA256 не означает новую проверку поведения мода.\n'
(W/'MANIFEST.md').write_text(manifest);shutil.copy2(W/'MANIFEST.md',OUT/'MANIFEST.md');shutil.copy2(W/'HANDOFF.md',OUT/'HANDOFF.md')
# Confirm every original source payload still matches the captured inventory.
changed=[]
for k,v in items.items():
 if H(Path(v['source']))!=v['sha256']:changed.append(k)
assert not changed,changed[:10]
(OUT/'source-unchanged.json').write_text(json.dumps({'sourceFilesRechecked':len(items),'changed':changed,'newBuildOrTestsRun':False},indent=2))
print('BACKUP_COMPLETE',json.dumps(checks),flush=True)
