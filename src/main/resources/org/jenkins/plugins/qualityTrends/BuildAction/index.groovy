package org.jenkins.plugins.qualityTrends.BuildAction

def l = namespace(lib.LayoutTagLib)
def st = namespace("jelly:stapler")

l.layout(title: _("Build Quality Trends")) {
    l.header() {
        st.bind(var: "it", value: my)
        script(src: "/plugin/quality-trends/qtBuildAction.js")
    }
    include(my.build, "sidepanel")
    l.main_panel {
        h1(_("Build Quality Trends"))
        div(id: "severity", style: "clear:both") {
            h2(_("Severity"))
            div(id: "severity_chart", style: "width:200px;height:200px;float:left;margin-right:10px")
            div(id: "severity_table", style: "float:left", class: "yui3-skin-sam")
        }
        div(id: "parsers", style: "clear:both; margin-top:10px") {
            h2(_("Parsers"))
            div(id: "parser_chart", style: "width:200px;height:200px;float:left;margin-right:10px")
            div(id: "parser_table", style: "float:left", class: "yui3-skin-sam")
        }
    }
}

