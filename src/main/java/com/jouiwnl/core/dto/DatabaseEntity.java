package com.jouiwnl.core.dto;

import java.io.Serializable;

public interface DatabaseEntity<PK> extends Serializable {
    PK getId();
}
