package php.java.bridge.generated;
public class JavaProxy {
    private static final String data = "<?php\n"+
"/* wrapper for Java.inc and PHPDebugger.inc */\n"+
"\n"+
"$java_include_only=str_pad(javaproxy_getHeader(\"X_JAVABRIDGE_INCLUDE_ONLY\", $_SERVER), 2, \"0\", false);\n"+
"if($java_include_only[1]=='1') { // include PHPDebugger.php\n"+
"	require_once(\"PHPDebugger.php\");\n"+
"}\n"+
"if($java_include_only[0]=='1') { // include Java.inc\n"+
"	require_once(\"Java.inc\");\n"+
"}\n"+
"\n"+
"if ($java_script_orig = $java_script = javaproxy_getHeader(\"X_JAVABRIDGE_INCLUDE\", $_SERVER)) {\n"+
"\n"+
"	if ($java_script!=\"@\") {\n"+
"		if (($_SERVER['REMOTE_ADDR']=='127.0.0.1') || (($java_script = realpath($java_script)) && (!strncmp($_SERVER['DOCUMENT_ROOT'], $java_script, strlen($_SERVER['DOCUMENT_ROOT']))))) {\n"+
"			chdir (dirname ($java_script));\n"+
"			if (     ($java_include_only[0]=='1') || \n"+
"					(($java_include_only[1]=='1') && !(isset($_SERVER[\"SCRIPT_FILENAME\"]) && isset($_SERVER[\"QUERY_STRING\"])&&!extension_loaded(\"Zend Debugger\")))) {\n"+
"					// if Java.inc is enabled or if PHPDebugger.php is enabled but not requested, require original script\n"+
"					// otherwise the debugger will load the file, if necessary.\n"+
"					require_once($java_script);\n"+
"			}\n"+
"		} else {\n"+
"			trigger_error(\"illegal access: \".$java_script_orig, E_USER_ERROR);\n"+
"		}\n"+
"	}\n"+
"\n"+
"	if ($java_include_only[0]=='1') { // Java.inc\n"+
"		java_call_with_continuation();\n"+
"	}\n"+
"	if ($java_include_only[1]=='1') { // PHPDebugger\n"+
"		$pdb_dbg->handleRequests();\n"+
"	}\n"+
"}\n"+
"function javaproxy_getHeader($name,$array) {\n"+
"	if (array_key_exists($name,$array)) return $array[$name];\n"+
"	$name=\"HTTP_$name\";\n"+
"	if (array_key_exists($name,$array)) return $array[$name];\n"+
"	return null;\n"+
"}\n"+
"\n"+
"?>\n"+"";
    public static final byte[] bytes = data.getBytes(); 
}
