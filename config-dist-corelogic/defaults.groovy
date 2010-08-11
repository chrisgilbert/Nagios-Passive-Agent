import org.apache.log4j.*
// Default property settings for npa
// All file paths are relative from the location of the npa.jar file.
//
npa {
    //URL for submission of nagios checks
    submit_url="https://secure.corelogic.co.uk"
    submit_port=443
    // Some settings for the HTTP authorisation
    submit_auth_server="secure.corelogic.co.uk"
    submit_auth_domain="Nagios Access"

    //Path for the Nagios cmd.cgi
    submit_path="/nagios/cgi-bin/cmd.cgi"
    //Nagios username
    submit_http_user="pcheck"
    //Nagios password
    submit_http_passwd="pch3ck1"
    // Flush queue timer
    flush_queue_ms=10000
    // How we submit results
    submit_method="http"
    // Number of failures before results are removed from the submission queue
    allowed_submit_failures=5

    // Proxy settings - uncomment username and password if requires authentication.
    proxy_auth_type="[basic OR none OR ntlm]"   // Should say "basic", "none" or "ntlm".  Basic will also work for digest authentication.
    proxy_port=                                 // Should be as an INTEGER (so no quotes)
    proxy_host="[proxy.hostname.co.uk]"         // Should be no http:// prefix and as a fully qualified name if using NTLM auth.
    //proxy_username="[proxy user]"             // No domain qualifier should be used with NTLM - instead the domain will be taken from the host FQDN.  Uncomment to use.
    //proxy_password="[proxy password]"         // It's a password in plaintext ;) Uncomment to use.
    proxy_enable=false                          // Switch to true to enable a proxy, otherwise leave as false

    // Daemon listener settings
    listen_port=9998

    //Config file to specify the checks
    configfile="/config/npa.xml"

    // Persistence settings for MetricsDB
    metrics_db_location="/db/metrics.db"
    metrics_db_purge_days=30
}

log4j {
    config_file="/config/log4j.xml"
}
