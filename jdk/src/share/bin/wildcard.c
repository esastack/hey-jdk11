/*
 * Copyright 2005-2006 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */

/*
 * Class-Path Wildcards
 *
 * The syntax for wildcards is a single asterisk. The class path
 * foo/"*", e.g., loads all jar files in the directory named foo.
 * (This requires careful quotation when used in shell scripts.)
 *
 * Only files whose names end in .jar or .JAR are matched.
 * Files whose names end in .zip, or which have a particular
 * magic number, regardless of filename extension, are not
 * matched.
 *
 * Files are considered regardless of whether or not they are
 * "hidden" in the UNIX sense, i.e., have names beginning with '.'.
 *
 * A wildcard only matches jar files, not class files in the same
 * directory.  If you want to load both class files and jar files from
 * a single directory foo then you can say foo:foo/"*", or foo/"*":foo
 * if you want the jar files to take precedence.
 *
 * Subdirectories are not searched recursively, i.e., foo/"*" only
 * looks for jar files in foo, not in foo/bar, foo/baz, etc.
 *
 * Expansion of wildcards is done early, prior to the invocation of a
 * program's main method, rather than late, during the class-loading
 * process itself.  Each element of the input class path containing a
 * wildcard is replaced by the (possibly empty) sequence of elements
 * generated by enumerating the jar files in the named directory.  If
 * the directory foo contains a.jar, b.jar, and c.jar,
 * e.g., then the class path foo/"*" is expanded into
 * foo/a.jar:foo/b.jar:foo/c.jar, and that string would be the value
 * of the system property java.class.path.
 *
 * The order in which the jar files in a directory are enumerated in
 * the expanded class path is not specified and may vary from platform
 * to platform and even from moment to moment on the same machine.  A
 * well-constructed application should not depend upon any particular
 * order.  If a specific order is required then the jar files can be
 * enumerated explicitly in the class path.
 *
 * The CLASSPATH environment variable is not treated any differently
 * from the -classpath (equiv. -cp) command-line option,
 * i.e. wildcards are honored in all these cases.
 *
 * Class-path wildcards are not honored in the Class-Path jar-manifest
 * header.
 *
 * Class-path wildcards are honored not only by the Java launcher but
 * also by most other command-line tools that accept class paths, and
 * in particular by javac and javadoc.
 *
 * Class-path wildcards are not honored in any other kind of path, and
 * especially not in the bootstrap class path, which is a mere
 * artifact of our implementation and not something that developers
 * should use.
 *
 * Classpath wildcards are only expanded in the Java launcher code,
 * supporting the use of wildcards on the command line and in the
 * CLASSPATH environment variable.  We do not support the use of
 * wildcards by applications that embed the JVM.
 */

#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h>
#include "java.h"       /* Strictly for PATH_SEPARATOR/FILE_SEPARATOR */
#include "jli_util.h"

#ifdef _WIN32
#include <windows.h>
#else /* Unix */
#include <unistd.h>
#include <dirent.h>
#endif /* Unix */

static int
exists(const char* filename)
{
#ifdef _WIN32
    return _access(filename, 0) == 0;
#else
    return access(filename, F_OK) == 0;
#endif
}

#define NEW_(TYPE) ((TYPE) JLI_MemAlloc(sizeof(struct TYPE##_)))

/*
 * Wildcard directory iteration.
 * WildcardIterator_for(wildcard) returns an iterator.
 * Each call to that iterator's next() method returns the basename
 * of an entry in the wildcard's directory.  The basename's memory
 * belongs to the iterator.  The caller is responsible for prepending
 * the directory name and file separator, if necessary.
 * When done with the iterator, call the close method to clean up.
 */
typedef struct WildcardIterator_* WildcardIterator;

#ifdef _WIN32
struct WildcardIterator_
{
    HANDLE handle;
    char *firstFile; /* Stupid FindFirstFile...FindNextFile */
};

static WildcardIterator
WildcardIterator_for(const char *wildcard)
{
    WIN32_FIND_DATA find_data;
    WildcardIterator it = NEW_(WildcardIterator);
    HANDLE handle = FindFirstFile(wildcard, &find_data);
    if (handle == INVALID_HANDLE_VALUE)
        return NULL;
    it->handle = handle;
    it->firstFile = find_data.cFileName;
    return it;
}

static char *
WildcardIterator_next(WildcardIterator it)
{
    WIN32_FIND_DATA find_data;
    if (it->firstFile != NULL) {
        char *firstFile = it->firstFile;
        it->firstFile = NULL;
        return firstFile;
    }
    return FindNextFile(it->handle, &find_data)
        ? find_data.cFileName : NULL;
}

static void
WildcardIterator_close(WildcardIterator it)
{
    if (it) {
        FindClose(it->handle);
        JLI_MemFree(it->firstFile);
        JLI_MemFree(it);
    }
}

#else /* Unix */
struct WildcardIterator_
{
    DIR *dir;
};

static WildcardIterator
WildcardIterator_for(const char *wildcard)
{
    DIR *dir;
    int wildlen = JLI_StrLen(wildcard);
    if (wildlen < 2) {
        dir = opendir(".");
    } else {
        char *dirname = JLI_StringDup(wildcard);
        dirname[wildlen - 1] = '\0';
        dir = opendir(dirname);
        JLI_MemFree(dirname);
    }
    if (dir == NULL)
        return NULL;
    else {
        WildcardIterator it = NEW_(WildcardIterator);
        it->dir = dir;
        return it;
    }
}

static char *
WildcardIterator_next(WildcardIterator it)
{
    struct dirent* dirp = readdir(it->dir);
    return dirp ? dirp->d_name : NULL;
}

static void
WildcardIterator_close(WildcardIterator it)
{
    if (it) {
        closedir(it->dir);
        JLI_MemFree(it);
    }
}
#endif /* Unix */

static int
equal(const char *s1, const char *s2)
{
    return JLI_StrCmp(s1, s2) == 0;
}

/*
 * FileList ADT - a dynamic list of C filenames
 */
struct FileList_
{
    char **files;
    int size;
    int capacity;
};
typedef struct FileList_ *FileList;

static FileList
FileList_new(int capacity)
{
    FileList fl = NEW_(FileList);
    fl->capacity = capacity;
    fl->files = (char **) JLI_MemAlloc(capacity * sizeof(fl->files[0]));
    fl->size = 0;
    return fl;
}



static void
FileList_free(FileList fl)
{
    if (fl) {
        if (fl->files) {
            int i;
            for (i = 0; i < fl->size; i++)
                JLI_MemFree(fl->files[i]);
            JLI_MemFree(fl->files);
        }
        JLI_MemFree(fl);
    }
}

static void
FileList_ensureCapacity(FileList fl, int capacity)
{
    if (fl->capacity < capacity) {
        while (fl->capacity < capacity)
            fl->capacity *= 2;
        fl->files = JLI_MemRealloc(fl->files,
                               fl->capacity * sizeof(fl->files[0]));
    }
}

static void
FileList_add(FileList fl, char *file)
{
    FileList_ensureCapacity(fl, fl->size+1);
    fl->files[fl->size++] = file;
}

static void
FileList_addSubstring(FileList fl, const char *beg, int len)
{
    char *filename = (char *) JLI_MemAlloc(len+1);
    memcpy(filename, beg, len);
    filename[len] = '\0';
    FileList_ensureCapacity(fl, fl->size+1);
    fl->files[fl->size++] = filename;
}

static char *
FileList_join(FileList fl, char sep)
{
    int i;
    int size;
    char *path;
    char *p;
    for (i = 0, size = 1; i < fl->size; i++)
        size += JLI_StrLen(fl->files[i]) + 1;

    path = JLI_MemAlloc(size);

    for (i = 0, p = path; i < fl->size; i++) {
        int len = JLI_StrLen(fl->files[i]);
        if (i > 0) *p++ = sep;
        memcpy(p, fl->files[i], len);
        p += len;
    }
    *p = '\0';

    return path;
}

static FileList
FileList_split(const char *path, char sep)
{
    const char *p, *q;
    int len = JLI_StrLen(path);
    int count;
    FileList fl;
    for (count = 1, p = path; p < path + len; p++)
        count += (*p == sep);
    fl = FileList_new(count);
    for (p = path;;) {
        for (q = p; q <= path + len; q++) {
            if (*q == sep || *q == '\0') {
                FileList_addSubstring(fl, p, q - p);
                if (*q == '\0')
                    return fl;
                p = q + 1;
            }
        }
    }
}

static int
isJarFileName(const char *filename)
{
    int len = JLI_StrLen(filename);
    return (len >= 4) &&
        (filename[len - 4] == '.') &&
        (equal(filename + len - 3, "jar") ||
         equal(filename + len - 3, "JAR")) &&
        /* Paranoia: Maybe filename is "DIR:foo.jar" */
        (JLI_StrChr(filename, PATH_SEPARATOR) == NULL);
}

static char *
wildcardConcat(const char *wildcard, const char *basename)
{
    int wildlen = JLI_StrLen(wildcard);
    int baselen = JLI_StrLen(basename);
    char *filename = (char *) JLI_MemAlloc(wildlen + baselen);
    /* Replace the trailing '*' with basename */
    memcpy(filename, wildcard, wildlen-1);
    memcpy(filename+wildlen-1, basename, baselen+1);
    return filename;
}

static FileList
wildcardFileList(const char *wildcard)
{
    const char *basename;
    FileList fl = FileList_new(16);
    WildcardIterator it = WildcardIterator_for(wildcard);
    if (it == NULL)
        return NULL;
    while ((basename = WildcardIterator_next(it)) != NULL)
        if (isJarFileName(basename))
            FileList_add(fl, wildcardConcat(wildcard, basename));
    WildcardIterator_close(it);
    return fl;
}

static int
isWildcard(const char *filename)
{
    int len = JLI_StrLen(filename);
    return (len > 0) &&
        (filename[len - 1] == '*') &&
        (len == 1 || IS_FILE_SEPARATOR(filename[len - 2])) &&
        (! exists(filename));
}

static void
FileList_expandWildcards(FileList fl)
{
    int i, j;
    for (i = 0; i < fl->size; i++) {
        if (isWildcard(fl->files[i])) {
            FileList expanded = wildcardFileList(fl->files[i]);
            if (expanded != NULL && expanded->size > 0) {
                JLI_MemFree(fl->files[i]);
                FileList_ensureCapacity(fl, fl->size + expanded->size);
                for (j = fl->size - 1; j >= i+1; j--)
                    fl->files[j+expanded->size-1] = fl->files[j];
                for (j = 0; j < expanded->size; j++)
                    fl->files[i+j] = expanded->files[j];
                i += expanded->size - 1;
                fl->size += expanded->size - 1;
                /* fl expropriates expanded's elements. */
                expanded->size = 0;
            }
            FileList_free(expanded);
        }
    }
}

const char *
JLI_WildcardExpandClasspath(const char *classpath)
{
    char *expanded;
    FileList fl;

    if (JLI_StrChr(classpath, '*') == NULL)
        return classpath;
    fl = FileList_split(classpath, PATH_SEPARATOR);
    FileList_expandWildcards(fl);
    expanded = FileList_join(fl, PATH_SEPARATOR);
    FileList_free(fl);
    if (getenv("_JAVA_LAUNCHER_DEBUG") != 0)
        printf("Expanded wildcards:\n"
               "    before: \"%s\"\n"
               "    after : \"%s\"\n",
               classpath, expanded);
    return expanded;
}

#ifdef DEBUG_WILDCARD
static void
FileList_print(FileList fl)
{
    int i;
    putchar('[');
    for (i = 0; i < fl->size; i++) {
        if (i > 0) printf(", ");
        printf("\"%s\"",fl->files[i]);
    }
    putchar(']');
}

static void
wildcardExpandArgv(const char ***argv)
{
    int i;
    for (i = 0; (*argv)[i]; i++) {
        if (equal((*argv)[i], "-cp") ||
            equal((*argv)[i], "-classpath")) {
            i++;
            (*argv)[i] = wildcardExpandClasspath((*argv)[i]);
        }
    }
}

static void
debugPrintArgv(char *argv[])
{
    int i;
    putchar('[');
    for (i = 0; argv[i]; i++) {
        if (i > 0) printf(", ");
        printf("\"%s\"", argv[i]);
    }
    printf("]\n");
}

int
main(int argc, char *argv[])
{
    argv[0] = "java";
    wildcardExpandArgv((const char***)&argv);
    debugPrintArgv(argv);
    /* execvp("java", argv); */
    return 0;
}
#endif /* DEBUG_WILDCARD */

/* Cute little perl prototype implementation....

my $sep = ($^O =~ /^(Windows|cygwin)/) ? ";" : ":";

sub expand($) {
  opendir DIR, $_[0] or return $_[0];
  join $sep, map {"$_[0]/$_"} grep {/\.(jar|JAR)$/} readdir DIR;
}

sub munge($) {
  join $sep,
    map {(! -r $_ and s/[\/\\]+\*$//) ? expand $_ : $_} split $sep, $_[0];
}

for (my $i = 0; $i < @ARGV - 1; $i++) {
  $ARGV[$i+1] = munge $ARGV[$i+1] if $ARGV[$i] =~ /^-c(p|lasspath)$/;
}

$ENV{CLASSPATH} = munge $ENV{CLASSPATH} if exists $ENV{CLASSPATH};
@ARGV = ("java", @ARGV);
print "@ARGV\n";
exec @ARGV;

*/
