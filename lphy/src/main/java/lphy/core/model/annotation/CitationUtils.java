package lphy.core.model.annotation;

import java.lang.annotation.Annotation;

public class CitationUtils {

    public static Citation getCitation(Class<?> c) {
        Annotation[] annotations = c.getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof Citation) {
                return (Citation) annotation;
            }
        }
        return null;
    }

    /**
     * @param citation {@link Citation}
     * @param etAl     e.g. "<i>et al</i>" for html
     * @return         the citation key, such as Drummond et. al. 2005
     */
    public static String getCitationKey(Citation citation, String etAl) {
        StringBuilder builder = new StringBuilder();
        String[] authors = citation.authors();
        if (authors.length > 2) {
            builder.append(authors[0]);
            builder.append(" ").append(etAl);
        } else {
            for (int i = 0; i < authors.length; i++) {
                if (i > 0) {
                    builder.append(" and ");
                }
                builder.append(authors[i]);
            }
        }
        builder.append(" ");
        builder.append(citation.year());
        return builder.toString();
    }

    private static String sanitizeDOI(String doi) {
        if (doi.startsWith("http")) return doi;
        if (doi.startsWith("doi.org")) return "https://" + doi;
        if (doi.length() > 0 && "01923456789".indexOf(doi.charAt(0)) >= 0) return "https://doi.org/" + doi;
        return doi;
    }

    /**
     * @param citation  {@link Citation}
     * @return a URL to this citation. If it has DOI, then use https://doi.org/,
     *         if it has ISBN, then use https://books.google.co.nz/books?vid=ISBN,
     *         otherwise return an empty string.
     */
    public static String getURL(Citation citation) {
        if (citation.DOI().length() >0)
            return sanitizeDOI(citation.DOI());
        // a book
        else if (citation.ISBN().length() >0)
            return "https://books.google.co.nz/books?vid=ISBN" + citation.ISBN();
        return "";
    }

}
