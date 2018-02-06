package com.here.gradle.plugins.jobdsl.util

/**
 * A comparator that orders path names. Files in the same directory are ordered alphabetically, and always come before
 * files in subdirectories. Subdirectories are also ordered alphabetically.
 */
class PathComparator implements Comparator<String> {
    @Override
    int compare(String o1, String o2) {
        def tokens1 = o1.tokenize('/')
        def tokens2 = o2.tokenize('/')
        for (int i = 0; i < Math.max(tokens1.size(), tokens2.size()); ++i) {
            def hasNext1 = i + 1 < tokens1.size()
            def hasNext2 = i + 1 < tokens2.size()
            def comp = tokens1[i] <=> tokens2[i]
            if (hasNext1 && hasNext2) {
                if (comp != 0) {
                    return comp
                }
            } else if (hasNext1) {
                return 1
            } else if (hasNext2) {
                return -1
            } else {
                return comp
            }
        }
    }
}
