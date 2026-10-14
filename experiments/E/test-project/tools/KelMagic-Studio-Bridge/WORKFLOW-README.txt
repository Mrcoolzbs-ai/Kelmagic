This is a ComfyUI editor-format workflow JSON (not API-export format). server.js converts this known classic-node workflow into the API prompt graph before POST /prompt.
Expected checkpoint: DreamShaper8_LCM.safetensors
Nodes: CheckpointLoaderSimple, CLIPTextEncode x2, KSampler, EmptyLatentImage, VAEDecode, SaveImage.
