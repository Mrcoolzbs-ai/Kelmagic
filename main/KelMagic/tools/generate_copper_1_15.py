"""Canonical Kel syllable atlas -> engraved copper, 4 native weather stages + wax variants."""
from pathlib import Path
from PIL import Image,ImageDraw,ImageFilter
import json,re,zipfile,hashlib
P=Path(__file__).resolve().parents[1];R=P/'src/main/resources';A=R/'assets/kelmagic';D=R/'data';canonical={w['word'] for w in json.loads((D/'kelmagic/magic/words.json').read_text())['words']};words=sorted(canonical);assert len(words)==180
z=zipfile.ZipFile(P/'build/moddev/artifacts/neoforge-21.1.256-client-extra-aka-minecraft-resources.jar');font=Image.open(A/'textures/font/kel.png').convert('RGBA');mapping=json.loads((A/'glyph-map.json').read_text());cw,ch=mapping['units']['cell'];cons='ptkbdgmnslvrj';vowels='aeiou';stages=['','exposed_','weathered_','oxidized_'];bases=['copper_block','exposed_copper','weathered_copper','oxidized_copper'];allids=[];oxid={};wax={};manifest={}
def js(p,d):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(d,ensure_ascii=False,indent=2)+'\n')
def blockid(word,stage,waxed=False):return ('waxed_' if waxed else '')+stages[stage]+'inscribed_copper_'+word
for word in words:
 units=[];i=0
 while i<len(word):
  c=cons.index(word[i]);v=0;i+=1
  if i<len(word) and word[i] in vowels:v=vowels.index(word[i])+1;i+=1
  index=c*6+v;units.append(index)
 tiles=[]
 for index in units:
  tile=font.crop(((index%8)*cw,(index//8)*ch,(index%8+1)*cw,(index//8+1)*ch)).getchannel('A');tile=tile.crop(tile.getbbox());tile=tile.resize((max(1,round(tile.width*36/tile.height)),36),Image.Resampling.LANCZOS);tiles.append(tile)
 total=sum(t.width for t in tiles)+4*(len(tiles)-1)
 if total>48:
  scale=(48-4*(len(tiles)-1))/sum(t.width for t in tiles);tiles=[t.resize((max(1,int(t.width*scale)),max(1,int(t.height*scale))),Image.Resampling.LANCZOS) for t in tiles]
 total=sum(t.width for t in tiles)+4*(len(tiles)-1);assert total<=48
 mask=Image.new('L',(64,64));x=(64-total)//2
 for tile in tiles:mask.paste(tile,(x,(64-tile.height)//2));x+=tile.width+4
 mask=mask.point(lambda a:255 if a>=96 else 0);full=[(x,y) for y in range(64) for x in range(64) if mask.getpixel((x,y))];coverage=[]
 for stage in range(3):
  import io
  image=Image.open(io.BytesIO(z.read('assets/minecraft/textures/block/'+bases[stage]+'.png'))).convert('RGBA').resize((64,64),Image.Resampling.NEAREST)
  # Preserve source stroke positions. Deterministic broad scratches remove lines, not random static.
  remain=Image.new('L',(64,64));outline=mask.filter(ImageFilter.MinFilter(3))
  for x,y in full:
   keep=stage==0 or (stage==1 and (y//6+x//12)%5 not in [1,3]) or (stage==2 and outline.getpixel((x,y))==0 and (y//9+x//15)%4==0)
   if keep:remain.putpixel((x,y),255)
  count=0
  for y in range(64):
   for x in range(64):
    if remain.getpixel((x,y)):
     base=image.getpixel((x,y));factor=[.28,.48,.79][stage];image.putpixel((x,y),tuple(int(v*factor) for v in base[:3])+(255,));count+=1
    elif x>0 and y>0 and remain.getpixel((x-1,y-1)):
     base=image.getpixel((x,y));boost=[22,12,2][stage];image.putpixel((x,y),tuple(min(255,v+boost) for v in base[:3])+(255,))
  coverage.append(count);path=A/f'textures/block/{blockid(word,stage)}.png';path.parent.mkdir(parents=True,exist_ok=True);image.save(path)
 assert coverage[0]>coverage[1]>coverage[2]>0,(word,coverage)
 for stage in range(4):
  normal=blockid(word,stage);texture='minecraft:block/oxidized_copper' if stage==3 else 'kelmagic:block/'+normal
  js(A/f'models/block/{normal}.json',{'parent':'minecraft:block/cube_all','textures':{'all':texture}})
  for waxed in [False,True]:
   id=blockid(word,stage,waxed);allids.append('kelmagic:'+id)
   if waxed:js(A/f'models/block/{id}.json',{'parent':'kelmagic:block/'+normal})
   js(A/f'blockstates/{id}.json',{'variants':{'':{'model':'kelmagic:block/'+id}}});js(A/f'models/item/{id}.json',{'parent':'kelmagic:block/'+id})
   input=('waxed_' if waxed else '')+bases[stage]
   js(D/f'kelmagic/recipe/{id}.json',{'type':'minecraft:stonecutting','ingredient':{'item':'minecraft:'+input},'result':{'id':'kelmagic:'+id,'count':1}})
   js(D/f'kelmagic/loot_table/blocks/{id}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':'kelmagic:'+id}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
   if not waxed:
    wax['kelmagic:'+id]='kelmagic:'+blockid(word,stage,True)
    if stage<3:oxid['kelmagic:'+id]='kelmagic:'+blockid(word,stage+1)
 manifest[word]={'units':[hex(0xE000+i) for i in units],'strokePixels':coverage+[0],'blocks':[blockid(word,s) for s in range(4)]}
for name,values in [('oxidizables',oxid),('waxables',wax)]:js(D/f'neoforge/data_maps/block/{name}.json',{'replace':False,'values':values})
for tag in ['mineable/pickaxe','needs_stone_tool']:js(D/f'minecraft/tags/block/{tag}.json',{'replace':False,'values':allids})
js(D/'kelmagic/tags/block/inscribed_copper.json',{'values':allids});js(D/'kelmagic/tags/item/inscribed_copper.json',{'values':allids})
for locale in ['ru_ru','en_us']:
 p=A/f'lang/{locale}.json';lang=json.loads(p.read_text());lang.pop('kelmagic.lore.mechanics',None);lang.pop('kelmagic.codex.mechanics',None)
 for word in words:
  for stage in range(4):
   for waxed in [False,True]:
    id=blockid(word,stage,waxed)
    if locale=='ru_ru':label=['Медный блок','Потемневший медный блок','Состаренный медный блок','Окисленный медный блок'][stage]+': '+word;label=('Вощёный '+label[0].lower()+label[1:]) if waxed else label
    else:label=('Waxed ' if waxed else '')+['Copper','Exposed copper','Weathered copper','Oxidized copper'][stage]+' inscription: '+word
    lang['block.kelmagic.'+id]=label
 lang['commands.kelmagic.copper_invalid']='Неизвестное слово или стадия меди.' if locale=='ru_ru' else 'Unknown copper word or stage.'
 lang['commands.kelmagic.copper_given']='Выдано медных надписей: %s' if locale=='ru_ru' else 'Copper inscriptions given: %s'
 p.write_text(json.dumps(lang,ensure_ascii=False,indent=2)+'\n')
js(A/'copper-glyphs.json',{'version':1,'glyphAtlasSHA256':hashlib.sha256((A/'textures/font/kel.png').read_bytes()).hexdigest(),'words':manifest})
print(f'{len(words)} canonical great-rite words;{len(allids)} native copper/waxed blocks;{len(oxid)} oxidation +{len(wax)}wax transitions;1440stonecutting recipes')
