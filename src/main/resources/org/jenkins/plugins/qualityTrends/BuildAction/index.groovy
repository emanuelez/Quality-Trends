package org.jenkins.plugins.qualityTrends.BuildAction

def l = namespace(lib.LayoutTagLib)

l.layout(title: _("Build Quality Trends")) {
    l.header() {
        include(my, "ieFix.jelly")
        script(src: "/plugin/quality-trends/jquery.js")
        script("jQuery.noConflict();")
        script(src: "/plugin/quality-trends/jquery.flot.js")
    }
    include(my.build, "sidepanel")
    l.main_panel {
        h1(_("Build Quality Trends"))
        h2(_("Build Summary"))
        div(id: "chart_div", style: "width:600px;height:300px;")
        script("""
jQuery(function () {
    var d2 = [[0, 3], [4, 8], [8, 5], [9, 13]];

    // a null signifies separate line segments
    var d3 = [[0, 12], [7, 12], null, [7, 2.5], [12, 2.5]];

    jQuery.plot(jQuery("#chart_div"), [ d2, d3 ]);
});""")
    }
}

