declare namespace h="http://marklogic.com/xdmp/harness"
declare namespace qm="http://marklogic.com/xdmp/query-meters"

define variable $NL { codepoints-to-string((10)) }

define variable $NAMES {
  ("Author", "Address", "Source", "Title", "Topic", "cauth")
}

define variable $QUERY_OPTS {
    "('case-insensitive', 'punctuation-insensitive', 'unwildcarded', 'lang=en')"
}

define function get-ms($msg as xs:string?) as xs:string {
  let $d := xdt:dayTimeDuration(xdmp:query-meters()/qm:elapsed-time)
  return text {
    $msg,
    3600 * fn:get-hours-from-dayTimeDuration($d)
    + 60 * fn:get-minutes-from-dayTimeDuration($d)
    + fn:get-seconds-from-dayTimeDuration($d),
    " elapsed ms"
  }
}

(: return a cts:word-query string for the input string
 :)
define function word-query-build($w as xs:string) as xs:string {
  concat(
    "cts:word-query('", $w, "', ", $NL, $QUERY_OPTS, ")"
  )
}

define function element-word-query-build
($e as element(), $names as xs:string*) as xs:string* {
  let $text := string($e)
  let $list :=
    for $n in $names
    return concat(
      "cts:element-word-query(",
      "xs:QName('", $n, "'), '", $text, "', ", $NL, $QUERY_OPTS,
      ")", $NL
    )
  return
    if (count($list) lt 2) then $list
    else concat("cts:or-query((", string-join($list, ", "), "))")
}

define function query-list-build($q as element()) as xs:string* {
  (: return a list of the elements that need to be searched :)
  typeswitch($q)
    case element(Address) return ("rp_composite", "rs_address")
    case element(Author) return ("primaryauthor", "author")
    case element(DocType) return ()
    case element(Lang) return ()
    case element(Source) return ("source_title", "source_abbrev")
    case element(Title) return ("item_title")
    case element(Topic)
      (: abstract text children are always in p :)
      return ("item_title", "p", "keyword", "keyplus")
    case element(cauth) return ("primaryauthor", "author")
    case element(cwork) return ()
    case element(Abstract) return ("AbstractText")
    case element(cyear) return ()
    case element(Discipline) return ()
    case element(CURSOR) return ()
    default return error(concat("UNKNOWN: ", xdmp:describe($q)))
}

(: return a cts:and-query string,
   containing every phrase that we will search for
 :)
define function query-build($q as element()) as xs:string? {
  let $list :=
    for $e in $q/element()
    return
      if (local-name($e) = ("AND", "OR", "NOT", "SAME", "NEAR"))
      then query-build($e)
      else concat("cts:word-query('",data($e),"',",$QUERY_OPTS,")")
(: simplifying above to use word-query()
      else element-word-query-build($e, query-list-build($e)) :)
  return
    if (empty($list)) then ()
    else if (count($list) = 1)
    then $list
    else concat(
      if (local-name($q) = ("OR"))
      then 'cts:or-query(('
      else if (local-name($q) = ("NOT"))
      then 'cts:and-query(('
      else 'cts:and-query((',
      $NL,
      string-join($list, ", "),
      '))', $NL
    )
}

(: return the correct query predicate(s) as a string
 :)
define function predicate-build($q as element()) as xs:string {
  (: handle any fixed element-value comparisons,
     and throw an error if we see an unknown element.
     TODO handle OR here too? doesn't seem to happen in samples.
   :)
  typeswitch($q)
    case element(AND)
      return string-join(
        for $e in $q/element() return predicate-build($e), ""
      )
    case element(SAME)
      return string-join(
        for $e in $q/element() return predicate-build($e), ""
      )
    case element(NOT)
      return string-join(
        for $e in $q/element() return predicate-build($e), ""
      )
    case element(NEAR)
      return string-join(
        for $e in $q/element() return predicate-build($e), ""
      )
    case element(OR) return ""
    case element(CURSOR) return ""
    case element(Discipline) return ""
    case element(Address) return ""
    case element(Author) return ""
    case element(DocType)
      return concat("[doctype/@code = '", string($q), "']", $NL)
    case element(Lang)
      return concat("[languages//@code = '", string($q), "']", $NL)
    case element(Source) return ""
    case element(Title) return ""
    case element(Topic) return ""
    case element(Abstract) return ""
    case element(cauth) return ""
    case element(cyear)
      return concat("[bib_issue/@year = '", string($q), "']", $NL)
    case element(cwork) return "" (: TODO :)
    default return error(concat("UNKNOWN: ", xdmp:describe($q)))
}

define function getQueries() as text()* {
        for $q in (doc(xdmp:get-request-field("uri","not-found"))/queries/query)

	let $pred := predicate-build($q/element())
	let $quer := query-build($q)
	let $final :=
	  if (empty(($pred, $quer)))
	  then error("empty query")
	  else text {
	    "cts:search(", $NL,
	    "input()/node()", $pred, ", ", $NL,
	    $quer, $NL,
	    ")", $NL
	  }
	(: ensure that the query is valid :)
	(: let $dummy := xdmp:eval(concat('xdmp:estimate(', $final, ')')) :)
	return $final
}
(: end :)
xdmp:document-insert(xdmp:get-request-field("dest"),
<h:script>
{
  for $query at $num in (getQueries())
  let $quoted := xdmp:quote($query)
  let $estimate := xdmp:quote('<estimate>{xdmp:estimate(')
  let $estimate-end := xdmp:quote(')}</estimate>')
  let $res := xdmp:quote('<search-results>')
  let $res-end := xdmp:quote('</search-results>')
  let $top := xdmp:quote('<top>{(')
  let $top-end := xdmp:quote(')[1 to 10]/node()}</top>')
  return
    <h:test>
      <h:name>{concat("TEST",xs:string($num))}</h:name>
      <h:comment-expected-result/>
      <h:set-up/>
      <h:query>{concat($res,$estimate,$quoted,$estimate-end,$top,$quoted,$top-end,$res-end)}</h:query>
      <h:tear-down/>
    </h:test>
}
</h:script>),
<done/>