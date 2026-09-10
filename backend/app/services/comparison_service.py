
import librosa
import numpy as np
import pyloudnorm

def compare_files(target_path: str, ref_path: str):
    # Cargar ambos
    y_t, sr_t = librosa.load(target_path, sr=None, mono=False)
    y_r, sr_r = librosa.load(ref_path, sr=None, mono=False)
    
    meter = pyloudnorm.Meter(sr_t)
    lufs_t = meter.integrated_loudness(y_t.T if y_t.ndim>1 else y_t)
    lufs_r = meter.integrated_loudness(y_r.T if y_r.ndim>1 else y_r)
    
    # Espectro comparativo por bandas
    # ... implementar con librosa.feature.melspectrogram
    
    return {
        "lufs_diff": float(lufs_t - lufs_r),
        "summary": f"Tu mezcla {lufs_t - lufs_r:.1f} dB vs referencia"
    }
