package com.zerlotemplate;

import com.zerlotemplate.Templateable;

public interface Snippet {
    public Templateable unbox();
    public String toString();
    public String info();
}
