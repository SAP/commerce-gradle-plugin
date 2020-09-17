
## What is the URL I should use for `supportPortalUrl` of the `SupportPortalDownload` task?

Search for "hybris commerce" Downloads in the SAP Support Portal, click on the version you want to use and use this URL.

1. Go to https://launchpad.support.sap.com/ and login
1. Switch the category to downloads (1) and search for the version (2) (use the search string `hybris <version>` to get the best result)
  ![Portal Search](images/portal-search.png)
1. Click on the "Maintenance Software Component" in the results (3) \
   *Make sure to use the "Maintenance Software Component", because only those contain a single download package!*
  ![Portal Result](images/portal-result.png)
1. Copy the URL (4). This is the `supportPortalUrl` you have to use for the task.
  ![Portal Link](images/portal-link.png)

## Where do I find the `sha256Sum` value for a SAP Commerce distribution in the SAP Support Portal?

Those can be found in the "Related Info" (1) -> "Content Info" (2), Field "Checksum" (3)

![Related Info](images/hash-related.png)

![Checksum](images/hash-checksum.png)
