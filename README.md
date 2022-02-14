# google-drive-api-clj

An easy-to-use command line application for working with Google Drive, using v3 of the API.

## Usage

This CLI application can be used directly by running its JAR file:

    $ java -jar google-drive-api-clj-0.1.0-standalone.jar [args]

Details on application arguments can be found in the help section of the CLI by running 
`java -jar google-drive-api-clj-0.1.0-standalone.jar --help`. The usual format for running the app is:
`java -jar google-drive-api-clj-0.1.0-standalone.jar --cfp <path_to_credentials.json_file> <subcommand> 
[--<subcommand_flag_argument>] [<subcommand_positional_argument]`

`cfp` stands for `credentials-file-path` and we can use it to specify path to the credentials.json file 
each time when we invoke a command. Alternatively, we can set the `GOOGLE_DRIVE_CREDENTIALS_ABSOLUTE_PATH`
environment variable and path to credentials file will be taken from its value.

## Options

Here we will list all functionalities this application has to offer:
1. Search through Shared Google Drive by terms.
2. Create a new directory on Google Drive.
3. Upload file to the root directory of Google Drive or to a specific
directory of your choice.
4. Move file from one directory to another.
5. Download file from Google Drive.
6. Delete a file or a directory.
7. Update metadata of a file or a directory

## License

Copyright © 2022 FIXME

This program and the accompanying materials are made available under the
terms of the Eclipse Public License 2.0 which is available at
http://www.eclipse.org/legal/epl-2.0.

This Source Code may also be made available under the following Secondary
Licenses when the conditions for such availability set forth in the Eclipse
Public License, v. 2.0 are satisfied: GNU General Public License as published by
the Free Software Foundation, either version 2 of the License, or (at your
option) any later version, with the GNU Classpath Exception which is available
at https://www.gnu.org/software/classpath/license.html.
