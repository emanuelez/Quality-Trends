package org.jenkins.plugins.qualityTrends.BuildAction

def l = namespace(lib.LayoutTagLib)
def st = namespace("jelly:stapler")

l.layout(title: _("Build Quality Trends")) {
    l.header() {
        st.bind(var: "it", value: my)
        link(rel: "stylesheet", type: "text/css", href: "/plugin/quality-trends/qtStyle.css")
        script(src: "/plugin/quality-trends/qtBuildAction.js")
    }
    include(my.build, "sidepanel")
    l.main_panel {
        h1(_("Build Quality Trends"))
        div(id: "severity", class: "qtsection") {
            h2(_("Severity"))
            div(id: "severity_chart", class: "qtchart")
            div(id: "severity_table", class: "yui3-skin-sam")
        }
        div(id: "parsers", class: "qtsection") {
            h2(_("Parsers"))
            div(id: "parser_chart", class: "qtchart")
            div(id: "parser_table", class: "yui3-skin-sam")
        }
        div(id: "orphans", class: "qtsection") {
            h2(_("Orphans"))
            div(id: "orphan_chart", class: "qtchart")
            div(id: "orphan_table", class: "yui3-skin-sam")
        }
        div(id: "entries", class: "qtsection") {
            h2(_("Entries"))
            div(id: "entry_pager_top", class: "entry_pager")
            div(id: "entry_table_container") {
                div(id: "entry_table", class: "yui3-skin-sam")
            }
            div(id: "entry_pager_bottom", class: "entry_pager")

        }
    }
}

