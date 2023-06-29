import numpy as np

def detect_peak(n1, n2, n3, n4, n5):

    max_norm = max(n1, n2, n3, n4, n5)
    min_norm = min(n1, n2, n3, n4, n5)

    if max_norm - min_norm > 0.2:
        return True
    return False
