package com.fondova.finance.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class NewsCategoryQueryBuilder {

    @Inject
    public NewsCategoryQueryBuilder() {
    }

    public String makeQuery(String keywords, boolean selectedAND) {
        String append = selectedAND ? "AND" : "OR";

        List<String> components = getComponents(keywords);
        return TextUtils.join(" " + append + " ", components);
    }

    public List<String> decompileQuery(String query) {
        List<String> components;

        if (query.matches(".*\\bAND\\b.*")) {
            components = Arrays.asList(query.split(" AND "));
        } else {
            components = Arrays.asList(query.split(" OR "));
        }

        for (int i = 0; i < components.size(); i++) {
            components.set(i, components.get(i).replace("\"", ""));
            components.set(i, components.get(i).replace("'", ""));
            components.set(i, components.get(i).replace("(", ""));
            components.set(i, components.get(i).replace(")", ""));
        }

        return components;
    }

    private List<String> getComponents(String keywords) {
        List<String> output = new ArrayList<>();
        String workingElement = "";
        String currentGroupOperator = "";

        String[] split = keywords.split(" ");

        for (String element : split) {
            if ((element.startsWith("\"") && element.endsWith("\"")) || (element.startsWith("'")
                    && element.endsWith("'")) || (element.startsWith("(") && element.endsWith(
                    ")"))) {
                output.add(element);
                continue;
            }

            if (element.startsWith("(")) {
                currentGroupOperator = ")";
                workingElement = element;
                continue;
            }
            if (element.startsWith("'")) {
                currentGroupOperator = "'";
                workingElement = element;
                continue;
            }
            if (element.startsWith("\"")) {
                currentGroupOperator = "\"";
                workingElement = element;
                continue;
            }
            if (workingElement.length() > 0) {

                workingElement += " " + element;
                if (element.endsWith(currentGroupOperator)) {
                    output.add(workingElement);
                    workingElement = "";
                }
            } else {
                output.add(element);
            }
        }
        return output;

    }
}
