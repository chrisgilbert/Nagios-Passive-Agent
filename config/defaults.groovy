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

    // Proxy settings
    proxy_auth_type="none"
    proxy_host="none"
    proxy_username="none"
    proxy_password="none"
    proxy_enable=false

    // Daemon listener settings
    listen_port=9998

    //Config file to specify the checks
    configfile="/../config/npa.xml"

    // Persistence settings for MetricsDB
    metrics_db_location="/../db/metrics.db"
    metrics_db_purge_days=30
}

log4j {
    config_file="/../config/log4j.xml"
}
