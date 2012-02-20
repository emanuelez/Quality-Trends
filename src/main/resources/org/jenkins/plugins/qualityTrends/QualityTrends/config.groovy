package org.jenkins.plugins.qualityTrends.QualityTrends

import org.jenkins.plugins.qualitytrends.model.Parser

def f = namespace(lib.FormTagLib)

f.section(title: _("Parsers")) {
    for (parser in Parser.all()) {
        f.entry(title: parser.name, field: parser.name) {
            f.checkbox(checked: instance.parsers.contains(parser))
        }
    }
}
