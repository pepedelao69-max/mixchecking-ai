
# Servicio de analisis pesado con librosa + pyloudnorm (Fase 2/3)
import librosa
import pyloudnorm
import numpy as np

def analyze_file(path: str):
    y, sr = librosa.load(path, sr=None, mono=False)
    # y shape (channels, samples) if stereo
    # Loudness con pyloudnorm ITU-R BS1770
    meter = pyloudnorm.Meter(sr)
    loudness = meter.integrated_loudness(y.T if y.ndim>1 else y)

    # RMS, peak
    rms = np.sqrt(np.mean(y**2))
    peak = np.max(np.abs(y))

    # Espectro, etc
    # TODO: completar con essentia y demucs para stems

    return {
        "sample_rate": sr,
        "integrated_lufs": float(loudness),
        "rms": float(rms),
        "peak": float(peak)
    }
