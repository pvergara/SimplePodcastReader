package com.ecos.collections;

import java.util.Stack;

public class BreadCrumbStack extends Stack<String> {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public BreadCrumbStack() {
        super();
    }

    @Override
    public String toString() {
        String result = "";
        StringBuilder sb = new StringBuilder();
        for (String item : this) {
            sb.append(item);
            sb.append("/");
        }
        result = sb.toString();

        if (result.length() >= 1)
            result = result.substring(0, result.length() - 1);
        return result;

    }

}
