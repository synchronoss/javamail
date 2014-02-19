
The source code in this directory was obtained from:
    http://kenai.com/projects/javamail/downloads/download/javamail-1.4.3-src.zip

The code was subsequently patched. The "patches" directory contains the patches
that were applied to the release source. To reapply a patch:

    cd patches
    patch -p1 < lz01.patch


PATCH NOTES

lz01: Patch JavaMail to support InterMail's invalid envelope fetch response
Resolves CLD-366 Getting "Problem fetching message headers" error when sorting on an inbox with more than 1000 messages.

