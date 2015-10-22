
The source code in this directory was obtained from:
    https://java.net/projects/javamail/downloads/download/source/javamail-1.5.2-src.zip

The code was subsequently patched. The "patches" directory contains the patches
that were applied to the release source. To reapply a patch:

    cd javamail/1.5.2
    patch -p1 < patches/lz01.patch


PATCH NOTES

lz01: Patch JavaMail to support InterMail's invalid envelope fetch response
Resolves CLD-366 Getting "Problem fetching message headers" error when sorting on an inbox with more than 1000 messages.

lz02: (obsolete) Added a hard-coded asumption that "pop.aol.com" supports UIDL.
No longer required because of the new "mail.pop3.disablecapa" session property introduced in JavaMail 1.4.4.

lz03: Use the command tag to tell the backend server whether to update the "last login" time.

owm06: Support an OWM system property to allow us to override the non-ASCII charsets on 'personal' name (MERCURY-575).

owm07: Avoid converting the 'personal' name if it fails (MERCURY-658).  Apply this patch after owm06.

omw08: Patch to fix IMAP proxy support.
