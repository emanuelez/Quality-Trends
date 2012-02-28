package org.jenkins.plugins.qualityTrends.BuildAction

def l = namespace(lib.LayoutTagLib)
def st = namespace("jelly:stapler")

l.layout(title: _("Build Quality Trends")) {
    l.header() {
        script(src: "/plugin/quality-trends/excanvas.js")
        script(src: "/plugin/quality-trends/jquery.js")
        script("jQuery.noConflict();")
        script(src: "/plugin/quality-trends/jquery.flot.js")
        script(src: "/plugin/quality-trends/jquery.flot.pie.js")

    }
    include(my.build, "sidepanel")
    l.main_panel {
        h1(_("Build Quality Trends"))
        h2(_("Build Summary"))
        div(id: "severity_chart", style: "width:600px;height:300px;text-align:center;background-color:#ddd") {
            text("loading...")
        }
        st.bind(var: "it", value: my)
        script("""
            jQuery(function () {

                it.getSeverities(function(t) {
                    sev = t.responseObject();

                    var data = [
                        {label: "Info", data: sev.infos, color: "#ffff66"},
                        {label: "Warnings", data: sev.warnings, color: "#ffcc00"},
                        {label: "Errors", data: sev.errors, color: "#cc0000"},
                        {label: "Orphans", data: sev.orphans, color: "#cccccc"}
                    ];

                    jQuery("#severity_chart").css("background-color", "#fff")

                    jQuery.plot(jQuery("#severity_chart"), data,
                        {
                            series: {
                                pie: {
                                    show: true
                                }
                            }
                        }
                    );
                });

            });""")
    }
}

