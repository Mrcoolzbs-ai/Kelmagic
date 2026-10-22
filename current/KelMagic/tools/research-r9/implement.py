from pathlib import Path
import json,copy
r=Path('src/main/resources');j=Path('src/main/java/dev/kelmagic')
def patch(p,a,b):
 s=p.read_text();assert a in s,(p,a);p.write_text(s.replace(a,b))
def load(p):return json.loads(p.read_text())
def save(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,ensure_ascii=False,indent=2)+'\n')
patch(Path('gradle.properties'),'1.29.0-r8-ink-drawings-TEST','1.30.0-r9-twilight-TEST')
p=j/'content/MagicGrove.java';patch(p,' public static final DeferredBlock<SaplingBlock> SAPLING=', ' public static final DeferredBlock<LeavesBlock> GOLDEN_LEAVES=block("golden_midnight_leaves",()->new LeavesBlock(copy(Blocks.OAK_LEAVES).lightLevel(s->9)));\n public static final DeferredBlock<SaplingBlock> SAPLING=')
patch(p,'return key.contains("midnight_")','return key.contains("midnight_")') if False else None
patch(j/'client/ResearchContentClient.java','MagicGrove.SAPLING,MagicGrove.LEAVES,','MagicGrove.SAPLING,MagicGrove.LEAVES,MagicGrove.GOLDEN_LEAVES,')
p=r/'data/kelmagic/worldgen/configured_feature/midnight_tree.json';v=load(p);c=v['config'];c['trunk_placer']={'type':'minecraft:mega_jungle_trunk_placer','base_height':26,'height_rand_a':2,'height_rand_b':2};state=c['foliage_provider']['state'];gold=copy.deepcopy(state);gold['Name']='kelmagic:golden_midnight_leaves';c['foliage_provider']={'type':'minecraft:weighted_state_provider','entries':[{'data':state,'weight':95},{'data':gold,'weight':5}]};c['foliage_placer']={'type':'minecraft:mega_jungle_foliage_placer','radius':3,'offset':0,'height':2};c['minimum_size'].update(lower_size=1,upper_size=2);save(p,v)
p=r/'data/minecraft/tags/block/leaves.json';v=load(p);v['values'].append('kelmagic:golden_midnight_leaves');save(p,v)
for name,parent in [('blockstates/golden_midnight_leaves',None),('models/block/golden_midnight_leaves','minecraft:block/leaves'),('models/item/golden_midnight_leaves','kelmagic:block/golden_midnight_leaves')]:
 save(r/f'assets/kelmagic/{name}.json',{'variants':{'':{'model':'kelmagic:block/golden_midnight_leaves'}}} if parent is None else {'parent':parent,**({'textures':{'all':'kelmagic:block/golden_midnight_leaves'}} if '/block/' in name else {})})
p=r/'data/kelmagic/loot_table/blocks/midnight_leaves.json';v=load(p);v['pools'][0]['entries'][0]['children'][0]['name']='kelmagic:golden_midnight_leaves';v['pools'][2]['entries'][0]={'type':'minecraft:item','name':'minecraft:golden_apple','conditions':[{'condition':'minecraft:survives_explosion'},{'condition':'minecraft:random_chance','chance':0.1}]};# no fortune multiplier, no shears/silk bonus
v['random_sequence']='kelmagic:blocks/golden_midnight_leaves';save(r/'data/kelmagic/loot_table/blocks/golden_midnight_leaves.json',v)
for loc,label in [('ru_ru','Золотистая сумеречная листва'),('en_us','Golden Twilight Leaves')]:
 p=r/f'assets/kelmagic/lang/{loc}.json';v=load(p);v['block.kelmagic.golden_midnight_leaves']=label;save(p,v)
for id in ['archivist_cabinet','manuscript_cabinet','sample_stand']:
 p=r/f'data/kelmagic/recipe/{id}.json';v=load(p);v['key']['P']={'item':'kelmagic:midnight_planks'};save(p,v)
p=r/'data/kelmagic/research/catalog.json';v=load(p)
for n in v['nodes']:
 if n['id'] in ['sample_stand','archivist_cabinet']:n['parents'].append('midnight_wood')
 if n['id']=='dragon_heart':n['tab']='great';n['x']=200;n['y']=5
 if n['id']=='sample_stand':n['x']=115;n['y']=40
 if n['id']=='archivist_cabinet':n['x']=15;n['y']=50
 if n['id']=='archivist_ink':n['x']=-90;n['y']=60
 if n['id']=='golden_wool':n['x']=-95;n['y']=30
 if n['id']=='midnight_wood':n['x']=20;n['y']=-20
 if n['id']=='living_fiber':n['x']=95;n['y']=45
 if n['id']=='resonance_station':n['x']=0;n['y']=-55
 if n['id']=='magic_sapling':n['x']=140;n['y']=-55
 if n['id']=='magic_ingot':n['x']=-60;n['y']=15
 if n['id']=='magic_cloth':n['x']=60;n['y']=15
 if n['id']=='mana_potion':n['x']=-115;n['y']=85
save(p,v)
p=j/'client/CodexScreen.java';patch(p,'Великие высказывания','Потусторонние знания');s=p.read_text();a=s.index('var route=ResearchLinkRoutes.route(nodes,from,n);');b=s.index('}for(var n:nodes){int xx=nx(n)',a);s=s[:a]+'wave(g,nx(from),ny(from),nx(n),ny(n),now,!entryLearned(n)&&!ResearchClient.state.started().contains(n.id()));}}'+s[b+1:];# restores original uninterrupted animated wave, no detours or segmented routes
# Strong new dependencies hide these two entries, even started older hypotheses, until wood discovery. Already learned stays usable.
s=s.replace('return creativeResearch||ResearchCatalog.learned(ResearchClient.state,n.id())||ResearchClient.state.started()', 'return creativeResearch||ResearchCatalog.learned(ResearchClient.state,n.id())||(!(n.id().equals("sample_stand")||n.id().equals("archivist_cabinet"))||ResearchCatalog.learned(ResearchClient.state,"midnight_wood"))&&(ResearchClient.state.started()')
s=s.replace('v->ResearchCatalog.learned(ResearchClient.state,v));}', 'v->ResearchCatalog.learned(ResearchClient.state,v)));}')
p.write_text(s)
patch(j/'item/CodexItem.java','getOrDefault(MagicComponents.CODEX_KNOWN.get(),false)','getOrDefault(MagicComponents.CODEX_KNOWN.get(),true)')
patch(j/'client/TableBookView.java','token,0,false,book.is','token,0,true,book.is')
p=j/'network/CodexOpen.java';patch(p,'this(w,n,h,0,0,"","",0,false);','this(w,n,h,0,0,"","",0,true);');patch(p,'this(w,n,h,s,p,q,t,0,false);','this(w,n,h,s,p,q,t,0,true);')
# Floor top edge points in the player's looking direction. Ceiling uses opposite base basis.
p=j/'content/InkDrawings.java';patch(p,'int color=ArchivistInkItem', 'int rotation=rotationFor(face,p.getDirection());\n  int color=ArchivistInkItem');patch(p,'marks.getFirst().inkColor()==color','marks.getFirst().inkColor()==color&&marks.getFirst().rotation()==rotation');patch(p,'face.getAxis().isVertical()?p.getDirection().get2DDataValue():0','rotation');patch(p,' public static void init(){}',' public static int rotationFor(Direction face,Direction looking){return face==Direction.UP?Math.floorMod(2-looking.get2DDataValue(),4):face==Direction.DOWN?looking.get2DDataValue():0;}\n public static void init(){}')
# nov uses same block/state to preserve containers, joined faces and player ownership data.
p=j/'content/ConnectedGlassBlock.java';patch(p,' public static final BooleanProperty[] JOIN',' public static final BooleanProperty VIOLET=BooleanProperty.create("violet");\n public static final BooleanProperty[] JOIN');patch(p,'var s=stateDefinition.any();','var s=stateDefinition.any().setValue(VIOLET,false);');patch(p,'b.add(JOIN);','b.add(JOIN).add(VIOLET);')
p=j/'archive/ArchiveTableBlock.java';patch(p,' public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING',' public static final net.minecraft.world.level.block.state.properties.BooleanProperty VIOLET=net.minecraft.world.level.block.state.properties.BooleanProperty.create("violet");\n public static final net.minecraft.world.level.block.state.properties.DirectionProperty FACING');patch(p,'stateDefinition.any().setValue(FACING','stateDefinition.any().setValue(VIOLET,false).setValue(FACING');patch(p,'b.add(FACING);','b.add(FACING,VIOLET);');patch(p,'12.686306820,18.061306820,0.850000000,14.913693180,20.288693180,1.950000000','13.150000000,18.250000000,0.850000000,14.450000000,20.100000000,1.950000000')
p=j/'server/MagicTransformation.java';patch(p,'var old=p.serverLevel().getBlockState(pos);boolean torch=', 'var old=p.serverLevel().getBlockState(pos);\n  net.minecraft.world.level.block.state.properties.BooleanProperty tint=old.is(dev.kelmagic.content.MagicGrove.GLASS.get())?dev.kelmagic.content.ConnectedGlassBlock.VIOLET:old.is(MagicArchive.TABLE.get())?dev.kelmagic.archive.ArchiveTableBlock.VIOLET:null;\n  if(tint!=null){if(!WorldSpells.place(p,pos,old.cycle(tint),true))return false;p.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,pos.getX()+.5,pos.getY()+.7,pos.getZ()+.5,10,.2,.2,.2,.02);return true;}\n  boolean torch=')
# Paired visual states
p=r/'assets/kelmagic/blockstates/resonance_glass.json';v=load(p);d={}
for k,m in v['variants'].items():
 d[k+',violet=false']=m;d[k+',violet=true']={'model':m['model'].replace('resonance_glass_','violet_resonance_glass_')}
save(p,{'variants':d})
for i in range(64):
 p=r/f'assets/kelmagic/models/block/resonance_glass_{i}.json';v=load(p);v['textures']={k:t.replace('resonance_glass_','violet_resonance_glass_') for k,t in v['textures'].items()};save(p.with_name(f'violet_resonance_glass_{i}.json'),v)
p=r/'assets/kelmagic/models/block/archivist_table.json';v=load(p);v['elements'][38].pop('rotation');save(p,v);vio=copy.deepcopy(v);vio['textures']['magic']='kelmagic:block/archive_magic_violet';save(p.with_name('archivist_table_violet.json'),vio)
p=r/'assets/kelmagic/blockstates/archivist_table.json';v=load(p);save(p,{'variants':{k+',violet='+str(t).lower():{**m,**({'model':'kelmagic:block/archivist_table_violet'} if t else {})} for k,m in v['variants'].items() for t in [False,True]}})
print('Implemented R9 mechanics/resources')
