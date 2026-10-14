from pathlib import Path
from PIL import Image,ImageFilter,ImageDraw,ImageFont
import numpy as np,math,json,subprocess,wave
r=Path(__file__).resolve().parents[1];a=r/'src/main/resources/assets/kelmagic'
# Keep the exact source-derived ink mask. Grade each stroke with a cyan core and warm gilded edging.
im=Image.open(r/'art/glyphs/kel-canonical-mask.png').convert('RGBA');arr=np.array(im);alpha=arr[:,:,3].copy();h,w=alpha.shape
Y,X=np.indices((h,w));t=(Y%72)/71
rgb=np.zeros((h,w,3),dtype=np.uint8)
for c,(top,mid,bottom) in enumerate([(255,124,37),(221,239,159),(139,255,204)]):
 rgb[:,:,c]=np.where(t<.4,top+(mid-top)*t/.4,mid+(bottom-mid)*(t-.4)/.6).clip(0,255)
# Thin luminous rim only, <= 1 pixel from canonical strokes; no invented marks.
mask=Image.fromarray(alpha);halo=np.array(mask.filter(ImageFilter.GaussianBlur(.65)))
outer=np.maximum(0,halo.astype(int)-alpha.astype(int));out=np.dstack([rgb,np.maximum(alpha,outer*0.55).clip(0,255).astype('uint8')]);Image.fromarray(out).save(a/'textures/font/kel.png')
proof=Image.new('RGB',(1000,440),'#091723');d=ImageDraw.Draw(proof)
font=ImageFont.truetype('/usr/share/fonts/msttcore/arial.ttf',22) if Path('/usr/share/fonts/msttcore/arial.ttf').exists() else ImageFont.load_default()
mp=json.loads((a/'glyph-map.json').read_text())['forms'];d.text((25,20),'KEL • canonical strokes / gilded cyan light',fill='#e8c58e',font=font)
for k,word in enumerate(['mina','tuna','sila','pir','no','lu']):
 x=30+(k%3)*320;y=80+(k//3)*170;d.text((x,y),word,fill='#b8e9f0',font=font);i=0;off=0
 while i<len(word):
  u=word[i];i+=1
  if i<len(word) and word[i] in 'aeiou':u+=word[i];i+=1
  cx,cy=mp[u]['cell'];tile=Image.fromarray(out).crop((cx*48,cy*72,(cx+1)*48,(cy+1)*72)).resize((72,108),Image.Resampling.NEAREST)
  proof.paste(tile,(x+off,y+25),tile);off+=72
proof.save(r/'docs/architecture/screenshots/kel-glyph-upgrade.png')
# Six original non-verbal resonances, deterministic synth: bell + airy shimmer, short attack, no spoken language.
sounds=a/'sounds/word';sounds.mkdir(parents=True,exist_ok=True);rate=44100
for i,f in enumerate([293.66,349.23,392,440,523.25,587.33]):
 dur=.64;ts=np.arange(int(rate*dur))/rate;rng=np.random.default_rng(2026+i)
 env=(1-np.exp(-ts*150))*np.exp(-ts*5.5)*(1-ts/dur)**2
 sig=sum(np.sin(2*np.pi*f*m*ts+.3*i)*np.exp(-ts*(1.3*m))*(1/m) for m in [1,2,3.01,4.97])
 sig+=(np.sin(2*np.pi*f*1.498*ts)*.26+rng.normal(0,.05,len(ts)))
 sig=(sig*env*.32).clip(-.92,.92);pcm=(sig*32767).astype('<i2')
 wav=r/'art/generated'/f'word{i+1}.wav'
 with wave.open(str(wav),'wb') as z:z.setnchannels(1);z.setsampwidth(2);z.setframerate(rate);z.writeframes(pcm.tobytes())
 subprocess.run(['ffmpeg','-y','-loglevel','error','-i',str(wav),'-c:a','libvorbis','-q:a','5',str(sounds/f'{i+1}.ogg')],check=True)
(a/'sounds.json').write_text(json.dumps({'word':{'subtitle':'subtitles.kelmagic.word','sounds':[{'name':f'kelmagic:word/{i+1}','weight':1,'volume':.8} for i in range(6)]}},indent=2)+'\n')
print('Canonical 78 forms styled; six original OGG sounds generated.')
