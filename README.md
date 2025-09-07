# Box Utils for Java

A set of basic utilities based on the [Box Java Gen SDK](https://github.com/box/box-java-sdk-gen).

## Dependencies

* Complied and tested with Java 17.

## Setup

As the utities uses the [Box API's](https://developer.box.com/reference/) a [Box Platform App](https://developer.box.com/guides/applications/app-types/platform-apps/).  Specifically a [JWT](https://developer.box.com/guides/authentication/jwt/) appolication.

When creating the app make sure the app is created, at a minimum, with the following ssettings and scopes:

* *App Access Level:* App + Enterprise
* *Application Scopes:*
    * Read all files and folders stored in Box
* *Advanced Features*
    * Make API Calls using the as-user header

Remember after creating the application to authorize it using the Admin Conssole and to download the App Settings JSON Config File as the location to this file is passed as a command line argument to the utilities.

Finally, be sure to add the platform app created as a collaborator to any and all folders it may need acccess to (or used the --user commandline argument)

## Reporter

Use to generate a CSV files based on a [metadata search](https://developer.box.com/reference/post-metadata-queries-execute-read/).

### Usage

```cmd
> reporter --help
usage: reporter [-h] -c CLIENT -r REPORT -o OUTPUT [-u USER]
                [--format CSV|XLSX]

named arguments:
  -h, --help             show this help message and exit
  -c CLIENT, --client CLIENT
                         Client configuration file path
  -r REPORT, --report REPORT
                         Report Definition File
  -o OUTPUT, --output OUTPUT
                         Output file path
  -u USER, --user USER   User ID to run the report for
  --format CSV|XLSX, -f CSV|XLSX
                         Format of the output report```

### Report Configuration Format

See the sample report configuration file in this distro (./etc/report-config.json.sample).  A annoted example is as follows.

```json
{
    // Template Name - required
    "template": "contract",
    // Optionall - defaults to "0"
    "ancestor_folder_id": "0",
    // Optional - see Metadate Search documentation for format
    "query": "autoRenew = :arg1",
    // Optional - see Metadate Search documentation for format
    // Special note - Dates MUST be specified in the format "YYYY-MM-DDT00:00:00Z" 
    "query_params": {
        "arg1": "Yes"
    },
    // Optional - see Metadate Search documentation for format
    "order_by": [
        {
            "field_key": "externalPartyName",
            "direction": "asc"
        }
    ],
    // Optional - Governer to limit output row count - default is no limit
    "limit": 100,
    // Optional - File properties to include in output - defaults to id, name
    "file_properties": [
        "id",
        "name",
        "size",
        "created_at",
        "modified_at"
    ],
    // Required - List of metadata template keys to include.
    "metadata_fields": [
        "externalPartyName",
        "contractType",
        "endDate",
        "autoRenew",
        "lawyer",
        "riskLevel"
    ],
    // Optional - Formatting string for Date Fields - Default is shown
    "date_format": "yyyy-MM-dd'T'HH:mm:ssXXX"
}
```