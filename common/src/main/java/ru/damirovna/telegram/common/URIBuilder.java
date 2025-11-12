package ru.damirovna.telegram.common;


public class URIBuilder {

    public static String paramToString(String[][] params) {
        if ((params == null) || (params.length == 0)) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String[] key : params) {
            stringBuilder.append(key[0] + "=" + key[1] + "&");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public static String getURIWithParams(String baseUri, String[][] params) {
        return baseUri + "?" + paramToString(params);
    }
}
