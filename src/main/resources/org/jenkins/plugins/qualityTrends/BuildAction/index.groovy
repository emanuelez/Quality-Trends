package org.jenkins.plugins.qualityTrends.BuildAction

def l = namespace(lib.LayoutTagLib)

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
        div(id: "chart_div", style: "width:600px;height:300px;")
        script(String.format("""
jQuery(function () {
    var data = [
        { label: "Info", data: %s, color: "#ffff66"},
        { label: "Warnings", data: %s, color: "#ffcc00"},
        { label: "Errors",  data: %s, color: "#cc0000"},
        { label: "Orphans",  data: %s, color: "#cccccc"}
    ];

    jQuery.plot(jQuery("#chart_div"), data,
        {
            series: {
                pie: {
                    show: true
                }
            }
        });
});""", my.infos, my.warnings, my.errors, my.orphans))
    }
}

