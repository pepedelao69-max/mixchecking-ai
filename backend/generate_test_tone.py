
import numpy as np
import soundfile as sf

sr=48000
t=np.arange(sr*5)/sr
tone=np.sin(2*np.pi*1000*t)*0.1 # -20dBFS aprox
stereo=np.stack([tone, tone*0.8]) # desbalance leve L/R
sf.write('/tmp/test_tone.wav', stereo.T, sr)
print("Test tone written")
