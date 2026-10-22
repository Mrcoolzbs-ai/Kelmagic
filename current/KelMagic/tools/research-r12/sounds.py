from pathlib import Path
import numpy as np,wave,json,subprocess
r=Path('src/main/resources/assets/kelmagic');p=r/'sounds/threshold';p.mkdir(parents=True,exist_ok=True);sr=22050
for index,(name,duration) in enumerate([('open',1.45),('close',.95),('travel',1.1),('hum',2.6)]):
 t=np.arange(int(sr*duration))/sr;x=t/duration;rng=np.random.default_rng(1200+index)
 # Original layered, nonverbal resonances, with intentionally different contours.
 if name=='open':freq=110+220*x*x;env=np.sin(np.pi*x)**.8
 elif name=='close':freq=330*(1-x)**2+65;env=(1-x)*np.minimum(x*25,1)
 elif name=='travel':freq=140+280*np.sin(np.pi*x);env=np.sin(np.pi*x)**1.4
 else:freq=98+4*np.sin(2*np.pi*x);env=np.sin(np.pi*x)**1.8
 phase=2*np.pi*np.cumsum(freq)/sr
 signal=sum(np.sin(phase*ratio+np.sin(2*np.pi*(.5+i*.2)*t)*.8)*amp for i,(ratio,amp) in enumerate([(1,.48),(1.498,.23),(2.003,.16),(2.511,.07)]))
 noise=np.convolve(rng.normal(0,1,len(t)),np.ones(30)/30,mode='same')
 signal=(signal+.1*noise)*env;signal/=max(1,np.max(np.abs(signal)));signal*=.62 if name!='hum' else .28
 wav=p/(name+'.wav')
 with wave.open(str(wav),'wb') as f:f.setnchannels(1);f.setsampwidth(2);f.setframerate(sr);f.writeframes((signal*32767).astype('<i2').tobytes())
 subprocess.run(['ffmpeg','-loglevel','error','-y','-i',str(wav),'-c:a','libvorbis','-q:a','5',str(p/(name+'.ogg'))],check=True);wav.unlink()
p=r/'sounds.json';j=json.loads(p.read_text())
for name in ['open','close','travel','hum']:j['threshold_'+name]={'subtitle':'subtitles.kelmagic.threshold_'+name,'sounds':[{'name':'kelmagic:threshold/'+name,'stream':False}]}
p.write_text(json.dumps(j,ensure_ascii=False,indent=2)+'\n')
