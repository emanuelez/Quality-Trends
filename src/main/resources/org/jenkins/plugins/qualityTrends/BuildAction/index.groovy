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
        div(id: "severity") {
            h2(_("Severity"))
            div(id: "severity_chart", style: "width:400px;height:300px;float:right;")
            div(id: "severity_table", class: "yui3-skin-sam")
        }
        st.bind(var: "it", value: my)
    }
}

