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
        script("""
jQuery(function () {
    var data = [
        { label: "Series1",  data: 10},
        { label: "Series2",  data: 30},
        { label: "Series3",  data: 90},
        { label: "Series4",  data: 70},
        { label: "Series5",  data: 80},
        { label: "Series6",  data: 110}
    ];

    jQuery.plot(jQuery("#chart_div"), data,
        {
            series: {
                pie: {
                    show: true
                }
            }
        });
});""")
    }
}

