package uk.gov.tna.tdr.fileformat

case class Identifiers(
    name: String,
    details: String
)

case class Matches(
    ns: String,
    id: String,
    format: String,
    version: String,
    mime: String,
    basis: String,
    warning: String
)

case class Files(
    filename: String,
    filesize: Double,
    modified: String,
    errors: String,
    matches: List[Matches]
)

case class Response(
    siegfried: String,
    scandate: String,
    signature: String,
    created: String,
    identifiers: List[Identifiers],
    files: List[Files]
)
