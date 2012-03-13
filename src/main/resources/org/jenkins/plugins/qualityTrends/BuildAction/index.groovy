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
            div(id: "severity_chart", style: "width:150px;height:150px;float:left;margin-right:10px")
            div(id: "severity_table", style: "float:left", class: "yui3-skin-sam")
        }
        div(id: "parsers", style: "clear:both; margin-top:10px") {
            h2(_("Parsers"))
            div(id: "parser_chart", style: "width:150px;height:150px;float:left;margin-right:10px")
            div(id: "parser_table", style: "float:left", class: "yui3-skin-sam")
        }
        div(id: "orphans", style: "clear:both; margin-top:10px") {
            h2(_("Orphans"))
            div(id: "orphan_chart", style: "width:150px;height:150px;float:left;margin-right:10px")
            div(id: "orphan_table", style: "float:left", class: "yui3-skin-sam")
        }
        div(id: "entries", style: "clear:both; margin-top:10px") {
            h2(_("Entries"))
            div(id: "entry_table_container", style: "float:left") {
                div(id: "entry_table", class: "yui3-skin-sam")
            }

        }
    }
}

