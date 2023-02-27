package php.java.bridge.generated;
public class PhpDebuggerPHP {
    private static final String data = "<?php /*-*- mode: php; tab-width:4 -*-*/\n"+
"\n"+
"  /**\n"+
"   * PHPDebugger.inc -- A PHP debugger for Eclipse for PHP Developers\n"+
"   *\n"+
"   * Copyright (C) 2009,2010 Jost Boekemeier.\n"+
"   *\n"+
"   * This file is part of the PHP/Java Bridge.\n"+
"   * \n"+
"   * The PHP/Java Bridge (\"the library\") is free software; you can\n"+
"   * redistribute it and/or modify it under the terms of the GNU General\n"+
"   * Public License as published by the Free Software Foundation; either\n"+
"   * version 2, or (at your option) any later version.\n"+
"   * \n"+
"   * The library is distributed in the hope that it will be useful, but\n"+
"   * WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
"   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n"+
"   * General Public License for more details.\n"+
"   * \n"+
"   * You should have received a copy of the GNU General Public License\n"+
"   * along with the PHP/Java Bridge; see the file COPYING.  If not, write to the\n"+
"   * Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA\n"+
"   * 02111-1307 USA.\n"+
"   * \n"+
"   * Linking this file statically or dynamically with other modules is\n"+
"   * making a combined work based on this library.  Thus, the terms and\n"+
"   * conditions of the GNU General Public License cover the whole\n"+
"   * combination.\n"+
"   * \n"+
"   * As a special exception, the copyright holders of this library give you\n"+
"   * permission to link this library with independent modules to produce an\n"+
"   * executable, regardless of the license terms of these independent\n"+
"   * modules, and to copy and distribute the resulting executable under\n"+
"   * terms of your choice, provided that you also meet, for each linked\n"+
"   * independent module, the terms and conditions of the license of that\n"+
"   * module.  An independent module is a module which is not derived from\n"+
"   * or based on this library.  If you modify this library, you may extend\n"+
"   * this exception to your version of the library, but you are not\n"+
"   * obligated to do so.  If you do not wish to do so, delete this\n"+
"   * exception statement from your version. \n"+
"   *\n"+
"   * Installation:\n"+
"   *\n"+
"   * Install \"Eclipse for PHP Developers\" version >= 3.5.2\n"+
"   * \n"+
"   * - Open Window -> Preferences -> Servers and set Default Web Server to: http://localhost:8080\n"+
"   * - Deploy JavaBridgeTemplate.war\n"+
"   * - Create a new Project using .../apache-tomcat-7.0.75/webapps/JavaBridgeTemplate as directory\n"+
"   * - Open index.php and start debugger: Default Web Server with Zend Debugger (other options default)\n"+
"   * - Click debug \n"+
"   *\n"+
"   * To debug standalone applications, remove zend_extension=ZendDebugger.so from your php.ini and set:\n"+
"   * \n"+
"   *<code>\n"+
"   * ;; activate the PHPDebugger in the php.ini\n"+
"   * auto_prepend_file=PHPDebugger.php\n"+
"   *</code>\n"+
"   *\n"+
"   * - Debug your PHP scripts as usual. \n"+
"   *\n"+
"   *\n"+
"   * @category   java\n"+
"   * @package    pdb\n"+
"   * @author     Jost Boekemeier\n"+
"   * @license    GPL+Classpath exception\n"+
"   * @version    7.0\n"+
"   * @link       http://php-java-bridge.sf.net/phpdebugger\n"+
"   */\n"+
"\n"+
"\n"+
"/** @access private */\n"+
"define (\"PDB_DEBUG\", 0);\n"+
"set_time_limit (0);\n"+
"if(!function_exists(\"token_get_all\")) {\n"+
"	dl(\"tokenizer.so\");\n"+
"}\n"+
"\n"+
"if ($pdb_script_orig = $pdb_script = pdb_getDebugHeader(\"X_JAVABRIDGE_INCLUDE\", $_SERVER)) {\n"+
"	if ($pdb_script!=\"@\") {\n"+
"		if (($_SERVER['REMOTE_ADDR']=='127.0.0.1') || (($pdb_script = realpath($pdb_script)) && (!strncmp($_SERVER['DOCUMENT_ROOT'], $pdb_script, strlen($_SERVER['DOCUMENT_ROOT']))))) {\n"+
"			$_SERVER['SCRIPT_FILENAME'] = $pdb_script; // set to the original script filename\n"+
"		} else {\n"+
"			trigger_error(\"illegal access: \".$pdb_script_orig, E_USER_ERROR);\n"+
"			unset($pdb_script);\n"+
"		}\n"+
"	}\n"+
"}\n"+
"\n"+
"\n"+
"\n"+
"if (!class_exists(\"pdb_Parser\")) {\n"+
"  /**\n"+
"   * The PHP parser\n"+
"   * @access private\n"+
"   */\n"+
"  class pdb_Parser {\n"+
"	const BLOCK = 1;\n"+
"	const STATEMENT = 2;\n"+
"	const EXPRESSION = 3;\n"+
"	const FUNCTION_BLOCK = 4; // BLOCK w/ STEP() as last statement\n"+
"\n"+
"	private $scriptName, $content;\n"+
"	private $code;\n"+
"	private $output;\n"+
"	private $line, $currentLine;\n"+
"	private $beginStatement, $inPhp, $inDQuote;\n"+
" \n"+
"	/**\n"+
"	 * Create a new PHP parser\n"+
"	 * @param string the script name\n"+
"	 * @param string the script content\n"+
"	 * @access private\n"+
"	 */\n"+
"	public function __construct($scriptName, $content) {\n"+
"	  $this->scriptName = $scriptName;\n"+
"	  $this->content = $content;\n"+
"	  $this->code = token_get_all($content);\n"+
"	  $this->output = \"\";\n"+
"	  $this->line = $this->currentLine = 0;\n"+
"	  $this->beginStatement = $this->inPhp = $this->inDQuote = false;\n"+
"	}\n"+
"\n"+
"	private function toggleDQuote($chr) {\n"+
"	  if ($chr == '\"') $this->inDQuote = !$this->inDQuote;\n"+
"	}\n"+
"\n"+
"	private function each() {\n"+
"	  $next = each ($this->code);\n"+
"	  if ($next) {\n"+
"		$cur = current($this->code);\n"+
"		if (is_array($cur)) {\n"+
"		  $this->currentLine = $cur[2] + ($cur[1][0] == \"\\n\" ? substr_count($cur[1], \"\\n\") : 0);\n"+
"		  if ($this->isWhitespace($cur)) {\n"+
"			$this->write($cur[1]);\n"+
"			return $this->each();\n"+
"		  }\n"+
"		}\n"+
"		else \n"+
"		  $this->toggleDQuote($cur);\n"+
"	  }\n"+
"	  return $next;\n"+
"	}\n"+
"\n"+
"	private function write($code) {\n"+
"	  //echo \"write:::\".$code.\"\\n\";\n"+
"	  $this->output.=$code;\n"+
"	}\n"+
"\n"+
"	private function writeInclude($once) {\n"+
"	  $name = \"\";\n"+
"	  while(1) {\n"+
"		if (!$this->each()) die(\"parse error\");\n"+
"		$val = current($this->code);\n"+
"		if (is_array($val)) {\n"+
"		  $name.=$val[1];\n"+
"		} else {\n"+
"		  if ($val==';') break;\n"+
"		  $name.=$val;\n"+
"		}\n"+
"	  }\n"+
"	  if (PDB_DEBUG == 2) \n"+
"		$this->write(\"EVAL($name);\");\n"+
"	  else\n"+
"		$this->write(\"eval('?>'.pdb_startInclude($name, $once)); pdb_endInclude();\");\n"+
"	}\n"+
"\n"+
"	private function writeCall() {\n"+
"	  while(1) {\n"+
"		if (!$this->each()) die(\"parse error\");\n"+
"		$val = current($this->code);\n"+
"		if (is_array($val)) {\n"+
"		  $this->write($val[1]);\n"+
"		} else {\n"+
"		  $this->write($val);\n"+
"		  if ($val=='{') break;\n"+
"		}\n"+
"	  }\n"+
"	  $scriptName = addslashes($this->scriptName);\n"+
"	  $this->write(\"\\$__pdb_CurrentFrame=pdb_startCall(\\\"$scriptName\\\", {$this->currentLine});\");\n"+
"	}\n"+
"\n"+
"	private function writeStep($pLevel) {\n"+
"	  $token = current($this->code);\n"+
"	  if ($this->inPhp && !$pLevel && !$this->inDQuote && $this->beginStatement && !$this->isWhitespace($token) && ($this->line != $this->currentLine)) {\n"+
"		$line = $this->line = $this->currentLine;\n"+
"		$scriptName = addslashes($this->scriptName);\n"+
"		if (PDB_DEBUG == 2)\n"+
"		  $this->write(\";STEP($line);\");\n"+
"		else\n"+
"		  $this->write(\";pdb_step(\\\"$scriptName\\\", $line, pdb_getDefinedVars(get_defined_vars(), (isset(\\$this) ? \\$this : NULL)));\");\n"+
"	  }\n"+
"	}\n"+
"\n"+
"	private function writeNext() {\n"+
"	  $this->next();\n"+
"	  $token = current($this->code);\n"+
"	  if (is_array($token)) $token = $token[1];\n"+
"	  $this->write($token);\n"+
"	}\n"+
"\n"+
"	private function nextIs($chr) {\n"+
"	  $i = 0;\n"+
"	  while(each($this->code)) {\n"+
"		$cur = current($this->code);\n"+
"		$i++;\n"+
"		if (is_array($cur)) {\n"+
"		  switch ($cur[0]) {\n"+
"		  case T_COMMENT:\n"+
"		  case T_DOC_COMMENT:\n"+
"		  case T_WHITESPACE:\n"+
"			break;	/* skip */\n"+
"		  default: \n"+
"			while($i--) prev($this->code);\n"+
"			return false;	/* not found */\n"+
"		  }\n"+
"		} else {\n"+
"		  while($i--) prev($this->code);\n"+
"		  return $cur == $chr;	/* found */\n"+
"		}\n"+
"	  }\n"+
"	  while($i--) prev($this->code);\n"+
"	  return false;	/* not found */\n"+
"	}\n"+
"\n"+
"	private function nextTokenIs($ar) {\n"+
"	  $i = 0;\n"+
"	  while(each($this->code)) {\n"+
"		$cur = current($this->code);\n"+
"		$i++;\n"+
"		if (is_array($cur)) {\n"+
"		  switch ($cur[0]) {\n"+
"		  case T_COMMENT:\n"+
"		  case T_DOC_COMMENT:\n"+
"		  case T_WHITESPACE:\n"+
"			break;	/* skip */\n"+
"		  default: \n"+
"			while($i--) prev($this->code);\n"+
"			return (in_array($cur[0], $ar));\n"+
"		  }\n"+
"		} else {\n"+
"		  break; /* not found */\n"+
"		}\n"+
"	  }\n"+
"	  while($i--) prev($this->code);\n"+
"	  return false;	/* not found */\n"+
"	}\n"+
"\n"+
"	private function isWhitespace($token) {\n"+
"	  $isWhitespace = false;\n"+
"	  switch($token[0]) {\n"+
"	  case T_COMMENT:\n"+
"	  case T_DOC_COMMENT:\n"+
"	  case T_WHITESPACE:\n"+
"		$isWhitespace = true;\n"+
"		break;\n"+
"	  }\n"+
"	  return $isWhitespace;\n"+
"	}\n"+
"	private function next() {\n"+
"	  if (!$this->each()) trigger_error(\"parse error\", E_USER_ERROR);\n"+
"	}\n"+
"\n"+
"	private function parseBlock () {\n"+
"	  $this->parse(self::BLOCK);\n"+
"	}\n"+
"	private function parseFunction () {\n"+
"	  $this->parse(self::FUNCTION_BLOCK);\n"+
"	}\n"+
"	private function parseStatement () {\n"+
"	  $this->parse(self::STATEMENT);\n"+
"	}\n"+
"	private function parseExpression () {\n"+
"	  $this->parse(self::EXPRESSION);\n"+
"	}\n"+
"\n"+
"	private function parse ($type) {\n"+
"	  pdb_Logger::debug(\"parse:::$type\");\n"+
"\n"+
"	  $this->beginStatement = true;\n"+
"	  $pLevel = 0;\n"+
"\n"+
"	  do {\n"+
"		$token = current($this->code);\n"+
"		if (!is_array($token)) {\n"+
"		  pdb_Logger::debug(\":::\".$token);\n"+
"		  if (!$pLevel && $type==self::FUNCTION_BLOCK && $token=='}') $this->writeStep($pLevel);\n"+
"		  $this->write($token);\n"+
"		  if ($this->inPhp && !$this->inDQuote) {\n"+
"			$this->beginStatement = false; \n"+
"			switch($token) {\n"+
"			case '(': \n"+
"			  $pLevel++;\n"+
"			  break;\n"+
"			case ')':\n"+
"			  if (!--$pLevel && $type==self::EXPRESSION) return;\n"+
"			  break;\n"+
"			case '{': \n"+
"			  $this->next();\n"+
"			  $this->parseBlock(); \n"+
"			  break;\n"+
"			case '}': \n"+
"			  if (!$pLevel) return;\n"+
"			  break;\n"+
"			case ';':\n"+
"			  if (!$pLevel) {\n"+
"				if ($type==self::STATEMENT) return;\n"+
"				$this->beginStatement = true; \n"+
"			  }\n"+
"			  break;\n"+
"			}\n"+
"		  }\n"+
"		} else {\n"+
"		  pdb_Logger::debug(\":::\".$token[1].\":(\".token_name($token[0]).')');\n"+
"\n"+
"		  if ($this->inDQuote) {\n"+
"			$this->write($token[1]);\n"+
"			continue;\n"+
"		  }\n"+
"\n"+
"		  switch($token[0]) {\n"+
"\n"+
"		  case T_OPEN_TAG: \n"+
"		  case T_START_HEREDOC:\n"+
"		  case T_OPEN_TAG_WITH_ECHO: \n"+
"			$this->beginStatement = $this->inPhp = true;\n"+
"			$this->write($token[1]);\n"+
"			break;\n"+
"\n"+
"		  case T_END_HEREDOC:\n"+
"		  case T_CLOSE_TAG: \n"+
"			$this->writeStep($pLevel);\n"+
"\n"+
"			$this->write($token[1]);\n"+
"			$this->beginStatement = $this->inPhp = false; \n"+
"			break;\n"+
"\n"+
"		  case T_FUNCTION:\n"+
"			$this->write($token[1]);\n"+
"			$this->writeCall();\n"+
"			$this->next();\n"+
"			$this->parseFunction();\n"+
"			$this->beginStatement = true;\n"+
"			break;\n"+
"\n"+
"		  case T_ELSE:\n"+
"			$this->write($token[1]);\n"+
"			if ($this->nextIs('{')) {\n"+
"			  $this->writeNext();\n"+
"			  $this->next();\n"+
"\n"+
"			  $this->parseBlock();\n"+
"			} else {\n"+
"			  $this->next();\n"+
"\n"+
"			  /* create an artificial block */\n"+
"			  $this->write('{');\n"+
"			  $this->beginStatement = true;\n"+
"			  $this->writeStep($pLevel);\n"+
"			  $this->parseStatement();\n"+
"			  $this->write('}');\n"+
"\n"+
"			}\n"+
"			if ($type==self::STATEMENT) return;\n"+
"\n"+
"			$this->beginStatement = true;\n"+
"			break;\n"+
"\n"+
"		  case T_DO:\n"+
"			$this->writeStep($pLevel);\n"+
"			$this->write($token[1]);\n"+
"			if ($this->nextIs('{')) {\n"+
"			  $this->writeNext();\n"+
"			  $this->next();\n"+
"\n"+
"			  $this->parseBlock();\n"+
"			  $this->next();\n"+
"\n"+
"			} else {\n"+
"			  $this->next();\n"+
"\n"+
"			  /* create an artificial block */\n"+
"			  $this->write('{');\n"+
"			  $this->beginStatement = true;\n"+
"			  $this->writeStep($pLevel);\n"+
"			  $this->parseStatement();\n"+
"			  $this->next();\n"+
"			  $this->write('}');\n"+
"			}\n"+
"			$token = current($this->code);\n"+
"			$this->write($token[1]);\n"+
"\n"+
"			if ($token[0]!=T_WHILE) trigger_error(\"parse error\", E_USER_ERROR);\n"+
"			$this->next();\n"+
"			$this->parseExpression();\n"+
"\n"+
"			if ($type==self::STATEMENT) return;\n"+
"\n"+
"			$this->beginStatement = true;\n"+
"			break;\n"+
"\n"+
"		  case T_CATCH:\n"+
"		  case T_IF:\n"+
"		  case T_ELSEIF:\n"+
"		  case T_FOR:\n"+
"		  case T_FOREACH:\n"+
"		  case T_WHILE:\n"+
"			$this->writeStep($pLevel);\n"+
"\n"+
"			$this->write($token[1]);\n"+
"			$this->next();\n"+
"\n"+
"			$this->parseExpression();\n"+
"\n"+
"			if ($this->nextIs('{')) {\n"+
"			  $this->writeNext();\n"+
"			  $this->next();\n"+
"\n"+
"			  $this->parseBlock();\n"+
"\n"+
"\n"+
"			} else {\n"+
"			  $this->next();\n"+
"			  /* create an artificial block */\n"+
"			  $this->write('{');\n"+
"			  $this->beginStatement = true;\n"+
"			  $this->writeStep($pLevel);\n"+
"			  $this->parseStatement();\n"+
"			  $this->write('}');\n"+
"			}\n"+
"\n"+
"			if ($this->nextTokenIs(array(T_ELSE, T_ELSEIF, T_CATCH))) {\n"+
"			  $this->beginStatement = false;\n"+
"			} else {\n"+
"			  if ($type==self::STATEMENT) return;\n"+
"			  $this->beginStatement = true;\n"+
"			}\n"+
"			break;\n"+
"\n"+
"		  case T_REQUIRE_ONCE:\n"+
"		  case T_INCLUDE_ONCE: \n"+
"		  case T_INCLUDE: \n"+
"		  case T_REQUIRE: \n"+
"			$this->writeStep($pLevel);\n"+
"			$this->writeInclude((($token[0]==T_REQUIRE_ONCE) || ($token[0]==T_INCLUDE_ONCE)) ? 1 : 0);\n"+
"\n"+
"			if ($type==self::STATEMENT) return;\n"+
"\n"+
"			$this->beginStatement = true;\n"+
"			break;\n"+
"\n"+
"		  case T_CLASS:\n"+
"			$this->write($token[1]);\n"+
"			$this->writeNext();\n"+
"			if ($this->nextIs('{')) {\n"+
"			  $this->writeNext();\n"+
"			  $this->next();\n"+
"			  $this->parseBlock(); \n"+
"			  $this->beginStatement = true;\n"+
"			} else {\n"+
"			  $this->writeNext();\n"+
"			  $this->beginStatement = false;\n"+
"			}\n"+
"			break;\n"+
"\n"+
"		  case T_CASE:\n"+
"		  case T_DEFAULT:\n"+
"		  case T_PUBLIC:\n"+
"		  case T_PRIVATE:\n"+
"		  case T_PROTECTED:\n"+
"		  case T_STATIC:\n"+
"		  case T_CONST:\n"+
"		  case T_GLOBAL:\n"+
"		  case T_ABSTRACT:\n"+
"			$this->write($token[1]);\n"+
"			$this->beginStatement = false;\n"+
"			break;\n"+
"\n"+
"		  default:\n"+
"			$this->writeStep($pLevel);\n"+
"			$this->write($token[1]);\n"+
"			$this->beginStatement = false;\n"+
"			break;\n"+
"	\n"+
"		  }\n"+
"		}\n"+
"	  } while($this->each());\n"+
"	}\n"+
"\n"+
"	/**\n"+
"	 * parse the given PHP script\n"+
"	 * @return the parsed PHP script\n"+
"	 * @access private\n"+
"	 */\n"+
"	public function parseScript() {\n"+
"	  do {\n"+
"		$this->parseBlock();\n"+
"	  } while($this->each());\n"+
"\n"+
"	  return $this->output;\n"+
"	}\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_Logger {\n"+
"  const FATAL = 1;\n"+
"  const INFO = 2;\n"+
"  const VERBOSE = 3;\n"+
"  const DEBUG = 4;\n"+
"\n"+
"  private static $logLevel = 0;\n"+
"  private static $logFileName;\n"+
"\n"+
"  private static function println($msg, $level) {\n"+
"	if (!self::$logLevel) self::$logLevel=PDB_DEBUG?self::DEBUG:self::INFO;\n"+
"	if ($level <= self::$logLevel) {\n"+
"	  static $file = null;\n"+
"	  if(!isset(self::$logFileName)) {\n"+
"		self::$logFileName = $_SERVER['HOME'].DIRECTORY_SEPARATOR.\"pdb_PHPDebugger.inc.log\";\n"+
"	  }\n"+
"	  if (!$file) $file = fopen(self::$logFileName, \"ab\") or die(\"fopen\");\n"+
"	  fwrite($file, time().\": \");\n"+
"	  fwrite($file, $msg.\"\\n\");\n"+
"	  fflush($file);\n"+
"	}\n"+
"  }\n"+
"\n"+
"  public static function logFatal($msg) {\n"+
"	self::println($msg, self::FATAL);\n"+
"  }\n"+
"  public static function logInfo($msg) {\n"+
"	self::println($msg, self::INFO);\n"+
"  }\n"+
"  public static function logMessage($msg) {\n"+
"	self::println($msg, self::VERBOSE);\n"+
"  }\n"+
"  public static function logDebug($msg) {\n"+
"	self::println($msg, self::DEBUG);\n"+
"  }\n"+
"  public static function debug($msg) {\n"+
"	self::logDebug($msg);\n"+
"  }\n"+
"  public static function log($msg) {\n"+
"	self::logMessage($msg);\n"+
"  }\n"+
"  public static function setLogLevel($level) {\n"+
"	self::$logLevel=$level;\n"+
"  }\n"+
"  public static function setLogFileName($name) {\n"+
"	self::$logFileName = $name;\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_Environment {\n"+
"  public $filename, $stepNext;\n"+
"  public $vars, $line, $firstLine;\n"+
"  public $parent;\n"+
"\n"+
"  public function __construct($parent, $filename, $stepNext, $firstLine) {\n"+
"	$this->parent = $parent;\n"+
"    $this->filename = $filename;\n"+
"    $this->stepNext = $stepNext;\n"+
"	$this->firstLine = $firstLine;\n"+
"    $this->line = -1;\n"+
"  }\n"+
"\n"+
"  public function update ($line, &$vars) {\n"+
"    $this->line = $line;\n"+
"    $this->vars = &$vars;\n"+
"  }\n"+
"  public function __toString() {\n"+
"	return \"pdb_Environment: {$this->filename}, {$this->firstLine} - {$this->line}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"abstract class pdb_Message {\n"+
"  public $session;\n"+
"\n"+
"  public abstract function getType();\n"+
"\n"+
"  public function __construct($session) {\n"+
"    $this->session = $session;\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $this->session->out->writeShort($this->getType());\n"+
"  }\n"+
"\n"+
"  private static $messages = array();\n"+
"  public static function register($message) {\n"+
"    pdb_Message::$messages[$message->getType()] = $message;\n"+
"  }\n"+
"  public function getMessageById($id) {\n"+
"    $message = pdb_Message::$messages[$id];\n"+
"    return $message;\n"+
"  }\n"+
"  public function getMessage() {\n"+
"    $id = $this->session->in->readShort();\n"+
"    $message = $this->getMessageById($id);\n"+
"    if (!$message) trigger_error(\"invalid message: $id\", E_USER_ERROR);\n"+
"    $message->deserialize();\n"+
"    return $message;\n"+
"  }\n"+
"\n"+
"  protected function handleContinueProcessFile($message) {\n"+
"	$code = $this->session->parseCode($this->currentFrame->filename, file_get_contents($this->currentFrame->filename));\n"+
"	if (PDB_DEBUG) pdb_Logger::debug( \"parse file:::\" . $code .\"\\n\");\n"+
"	if (!PDB_DEBUG) ob_start();\n"+
"	self::doEval ($code);\n"+
"	$output = $this->getMessageById(pdb_OutputNotification::TYPE);\n"+
"	if(!PDB_DEBUG) $output->setOutput(ob_get_contents());\n"+
"	if(!PDB_DEBUG) ob_end_clean();\n"+
"	$output->serialize();\n"+
"	$this->status = 42; //FIXME\n"+
"	$this->getMessageById(pdb_DebugScriptEndedNotification::TYPE)->serialize();\n"+
"    return true;\n"+
"  }\n"+
"  private static function doEval($__pdb_Code) {\n"+
"    return  eval (\"?>\".$__pdb_Code);\n"+
"  }\n"+
"  protected function handleStep($message) {\n"+
"    return false;\n"+
"  }\n"+
"  protected function handleGo($message) {\n"+
"    foreach ($this->session->allFrames as $frame) {\n"+
"      $frame->stepNext = false;\n"+
"    }\n"+
"    return true; // exit\n"+
"  }\n"+
"  public function handleRequests () {\n"+
"	$this->ignoreInterrupt = false;\n"+
"\n"+
"    $this->serialize();\n"+
"    while(1) {\n"+
"      $message = $this->getMessage();\n"+
"      switch ($message->getType()) {\n"+
"      case pdb_SetProtocolRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_StartRequest::TYPE:\n"+
"		$message->ack();\n"+
"		$this->getMessageById(pdb_StartProcessFileNotification::TYPE)->serialize();\n"+
"		break;\n"+
"      case pdb_ContinueProcessFileNotification::TYPE:\n"+
"		if ($this->handleContinueProcessFile($message)) return pdb_ContinueProcessFileNotification::TYPE;\n"+
"		break;\n"+
"      case pdb_AddBreakpointRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_RemoveBreakpointRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_RemoveAllBreakpointsRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_GetCallStackRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_GetCWDRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_GetVariableValueRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_AddFilesRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_FileContentExtendedRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_MsgEvalRequest::TYPE:\n"+
"		$message->ack();\n"+
"		break;\n"+
"      case pdb_GoRequest::TYPE:\n"+
"		$message->ack();\n"+
"		if ($this->handleGo($message)) return pdb_GoRequest::TYPE;\n"+
"		break;\n"+
"      case pdb_StepOverRequest::TYPE:\n"+
"		$message->ack();\n"+
"		if ($this->handleStep($message)) return pdb_StepOverRequest::TYPE;\n"+
"		break;\n"+
"      case pdb_StepIntoRequest::TYPE:\n"+
"		$message->ack();\n"+
"		if ($this->handleStep($message)) return pdb_StepIntoRequest::TYPE;\n"+
"		break;\n"+
"      case pdb_StepOutRequest::TYPE:\n"+
"		$message->ack();\n"+
"		if ($this->handleStep($message)) return pdb_StepOutRequest::TYPE;\n"+
"		break;\n"+
"      case pdb_End::TYPE:\n"+
"		$this->session->end();\n"+
"      default: trigger_error(\"protocol error: $message\", E_USER_ERROR);\n"+
"      }\n"+
"    }\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"abstract class pdb_MessageRequest extends pdb_Message {\n"+
"  public abstract function ack();\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_Serializer {\n"+
"  private $serial;\n"+
"  private $depth;\n"+
"\n"+
"  private function doSerialize ($o, $depth) {\n"+
"    $serial = &$this->serial;\n"+
"\n"+
"    switch(gettype($o)) {\n"+
"    case 'object':\n"+
"      $serial.=\"O:\";\n"+
"      $serial.=strlen(get_class($o));\n"+
"      $serial.=\":\\\"\";\n"+
"      $serial.=get_class($o);\n"+
"      $serial.=\"\\\":\";\n"+
"      $serial.=count((array)$o);\n"+
"\n"+
"	  if ($depth <= $this->depth) {\n"+
"		$serial.=\":{\";\n"+
"		foreach((array)$o as $k=>$v) {\n"+
"		  $serial.=serialize($k);\n"+
"		  $this->doSerialize($v, $depth+1);\n"+
"		}\n"+
"		$serial.=\"}\";\n"+
"	  } else {\n"+
"		$serial .= \";\";\n"+
"	  }\n"+
"      break;\n"+
"\n"+
"    case 'array':\n"+
"      $serial.=\"a:\";\n"+
"      $serial.=count($o);\n"+
"\n"+
"	  if ($depth <= $this->depth) {\n"+
"		$serial.=\":{\";\n"+
"		foreach($o as $k=>$v) {\n"+
"		  $serial.=serialize($k);\n"+
"		  $this->doSerialize($v, $depth+1);\n"+
"		}\n"+
"		$serial.=\"}\";\n"+
"	  } else {\n"+
"		$serial.=\";\";\n"+
"	  }\n"+
"      break;\n"+
"    default:\n"+
"      $serial.=serialize($o);\n"+
"      break;\n"+
"    }\n"+
"  }\n"+
"\n"+
"  public function serialize ($obj, $depth) {\n"+
"    $this->serial = \"\";\n"+
"	$this->depth = $depth;\n"+
"\n"+
"    $this->doSerialize ($obj, 1);\n"+
"\n"+
"    return $this->serial;\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_DebugSessionStart extends pdb_Message {\n"+
"  const TYPE = 2005;\n"+
"\n"+
"  public $status;\n"+
"  public $end;\n"+
"\n"+
"  private $breakFirstLine;\n"+
"  private $enable;\n"+
"  public $uri;\n"+
"  public $query;\n"+
"  public $options;\n"+
"  \n"+
"  public $in, $out;\n"+
"  private $outputNotification;\n"+
"\n"+
"  public $lines;\n"+
"  public $breakpoints;\n"+
"\n"+
"  public $currentTopLevelFrame, $currentFrame;\n"+
"  public $allFrames; // should be a weak map so that frames could be gc'ed\n"+
"\n"+
"  public $ignoreInterrupt; \n"+
"\n"+
"  public $serializer;\n"+
"\n"+
"  public $includedScripts;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function __construct($options) {\n"+
"    parent::__construct($this);\n"+
"	$this->end = true;\n"+
"	if (isset($_SERVER[\"SCRIPT_FILENAME\"]) && isset($_SERVER[\"QUERY_STRING\"])&&!extension_loaded(\"Zend Debugger\")) {\n"+
"	  $filename = $uri = $_SERVER[\"SCRIPT_FILENAME\"];\n"+
"	  $queryStr = $_SERVER[\"QUERY_STRING\"];\n"+
"	} else { // PHPDebugger disabled\n"+
"	  global $pdb_script;\n"+
"	  $this->enable = false;\n"+
"	  if (isset($pdb_script) && $pdb_script!=\"@\") {\n"+
"	  	require_once($pdb_script);\n"+
"	  }\n"+
"	  return;\n"+
"	}\n"+
"\n"+
"	$params = explode('&', $queryStr);\n"+
"	$args = array();\n"+
"	for ($i=0; $i<count($params); $i++) {\n"+
"		$arg=explode( '=', urldecode($params[$i]));\n"+
"		$args[$arg[0]] = $arg[1];\n"+
"	}\n"+
"	$this->enable = $args[\"start_debug\"];\n"+
"	$this->breakFirstLine = isset($args[\"debug_stop\"]) ? $args[\"debug_stop\"] : 0;\n"+
"    $this->uri = $uri;\n"+
"    $this->query = $queryStr;\n"+
"    $this->options = $options;\n"+
"    $this->breakpoints = $this->lines = array();\n"+
"\n"+
"	$this->serializer = new pdb_Serializer();\n"+
"\n"+
"	$this->currentTopLevelFrame = $this->currentFrame = new pdb_Environment(null, $filename, false, 1);\n"+
"	$this->allFrames[] = $this->currentFrame;\n"+
"	$this->ignoreInterrupt = false;\n"+
"	$this->includedScripts = array();\n"+
"\n"+
"    $errno = 0; $errstr = \"\";\n"+
"    $io = null;\n"+
"    foreach(explode(\",\", $args[\"debug_host\"]) as $host) {\n"+
"        if ($io = fsockopen($host, $args['debug_port'], $errno, $errstr, 5)) {\n"+
"            break;\n"+
"        }\n"+
"    }\n"+
"    if ($io==null) {\n"+
"        trigger_error(\"fsockopen\", E_USER_ERROR);\n"+
"    }\n"+
"	$this->end = false;\n"+
"\n"+
"    $this->in =new pdb_In($io, $this);\n"+
"    $this->out=new pdb_Out($io, $this);\n"+
"  }\n"+
"  public function end() {\n"+
"	$this->end = true;\n"+
"	if (PDB_DEBUG) pdb_Logger::debug( \"end() called\");\n"+
"	exit(0);\n"+
"  }\n"+
"  /**\n"+
"   * @access private\n"+
"   */\n"+
"  public function flushOutput() {\n"+
"	if (!isset($this->outputNotification))\n"+
"	  $this->outputNotification = $this->getMessageById(pdb_OutputNotification::TYPE);\n"+
"\n"+
"	$this->outputNotification->setOutput(ob_get_contents());\n"+
"	if (!PDB_DEBUG) ob_clean();\n"+
"	$this->outputNotification->serialize();\n"+
"  }\n"+
"\n"+
"  /**\n"+
"   * @access private\n"+
"   */\n"+
"  public function resolveIncludePath($scriptName) {\n"+
"	if (file_exists($scriptName)) return realpath($scriptName);\n"+
"	$paths = explode(PATH_SEPARATOR, get_include_path());\n"+
"	$name = $scriptName;\n"+
"	foreach ($paths as $path) {\n"+
"	  $scriptName = realpath(\"${path}${name}\");\n"+
"	  if ($scriptName) return $scriptName;\n"+
"	}\n"+
"	trigger_error(\"file $scriptName not found\", E_USER_ERROR);\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt(2004102501);\n"+
"    $out->writeString($this->currentFrame->filename);\n"+
"    $out->writeString($this->uri);\n"+
"    $out->writeString($this->query);\n"+
"    $out->writeString($this->options);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function handleRequests () {\n"+
"	if ($this->enable) {\n"+
"	  set_error_handler(\"pdb_error_handler\");\n"+
"	  register_shutdown_function(\"pdb_shutdown\");\n"+
"\n"+
"	  parent::handleRequests(); \n"+
"	  if (PDB_DEBUG) pdb_Logger::debug( \"exit({$this->status})\");\n"+
"	  exit ($this->status); }\n"+
"  }\n"+
"  public function hasBreakpoint($scriptName, $line) {\n"+
"	if ($this->breakFirstLine) {$this->breakFirstLine = false; return true;}\n"+
"\n"+
"    if ($this->currentFrame->stepNext) return true;\n"+
"\n"+
"    foreach ($this->breakpoints as $breakpoint) {\n"+
"      if($breakpoint->type==1) {\n"+
"		if ($breakpoint->file==$scriptName&&$breakpoint->line==$line) return true;\n"+
"      }\n"+
"    }\n"+
"\n"+
"    return false;\n"+
"  }\n"+
"  function parseCode($filename, $contents) {\n"+
"	$parser = new pdb_Parser($filename, $contents);\n"+
"	return $parser->parseScript();\n"+
"  }\n"+
"\n"+
"  public function __toString() {\n"+
"    return \"pdb_DebugSessionStart: {$this->currentFrame->filename}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_HeaderOutputNotification extends pdb_Message {\n"+
"  const TYPE = 2008;\n"+
"  private $out;\n"+
"\n"+
"  public function setOutput($out) {\n"+
"    $this->out = $out;\n"+
"  }\n"+
"  protected function getAsciiOutput() {\n"+
"    return $this->out;\n"+
"  }\n"+
"  protected function getEncodedOutput () {\n"+
"    return $this->out; //FIXME\n"+
"  }\n"+
"  protected function getOutput() {\n"+
"    return $this->getAsciiOutput();\n"+
"  }\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeString($this->getOutput());\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_HeaderOutputNotification: \".$this->getOutput();\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_OutputNotification extends pdb_HeaderOutputNotification {\n"+
"  const TYPE = 2004;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  protected function getOutput() {\n"+
"    return $this->getEncodedOutput();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_OutputNotification: \".$this->getAsciiOutput();\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_ErrorNotification extends pdb_Message {\n"+
"  const TYPE = 2006;\n"+
"  private $type, $filename, $lineno, $error;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function setError($type, $filename, $lineno, $error) {\n"+
"	$this->type = $type;\n"+
"	$this->filename = $filename;\n"+
"	$this->lineno = $lineno;\n"+
"	$this->error = $error;\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->type);\n"+
"	$out->writeString($this->filename);\n"+
"	$out->writeInt($this->lineno);\n"+
"	$out->writeString($this->error);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_ErrorNotification: {$this->error} at {$this->filename} line {$this->lineno}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_DebugScriptEndedNotification extends pdb_Message {\n"+
"  const TYPE = 2002;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeShort($this->session->status);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_DebugScriptEndedNotification: {$this->session->status}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_ReadyNotification extends pdb_Message {\n"+
"  const TYPE = 2003;\n"+
"  \n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"\n"+
"  protected function handleStep($message) {\n"+
"    return true;\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeString($this->session->currentFrame->filename);\n"+
"    $out->writeInt($this->session->currentFrame->line);\n"+
"    $out->writeInt(0);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_ReadyNotification: {$this->session->currentFrame->filename}, {$this->session->currentFrame->line}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_SetProtocolRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 10000;\n"+
"  public $id;\n"+
"  public $protocolId;\n"+
"  \n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    $this->protocolId = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_SetProtocolResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_SetProtocolRequest: \". $this->protocolId;\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_SetProtocolResponse extends pdb_Message {\n"+
"  const TYPE = 11000;\n"+
"  private $req;\n"+
"  \n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"\n"+
"    // use fixed id instead of $out->writeInt($this->req->protocolId);\n"+
"	$out->writeInt(2012121702);\n"+
"\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_SetProtocolResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StartRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 1;\n"+
"  public $id;\n"+
"  public $protocolId;\n"+
"  \n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"\n"+
"  public function ack() {\n"+
"    $res = new pdb_StartResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StartRequest: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_AddFilesRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 38;\n"+
"  public $id;\n"+
"  public $pathSize;\n"+
"  public $paths;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    $this->pathSize = $in->readInt();\n"+
"    $this->paths = array();\n"+
"    for($i=0; $i<$this->pathSize; $i++) {\n"+
"       $this->paths[] = $in->readString();\n"+
"    }\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"\n"+
"  public function ack() {\n"+
"    $res = new pdb_AddFilesResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_AddFilesRequest: \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_FileContentExtendedRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 10002;\n"+
"  public $id;\n"+
"  public $size;\n"+
"  public $checksum;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    $this->size = $in->readInt();\n"+
"    $this->checksum = $in->readInt();\n"+
"\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"\n"+
"  public function ack() {\n"+
"    $res = new pdb_FileContentExtendedResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_FileContentExtendedRequest: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_AddFilesResponse extends pdb_Message {\n"+
"  const TYPE = 1038;\n"+
"  private $req;\n"+
"  \n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt(0);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_AddFilesResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_FileContentExtendedResponse extends pdb_Message {\n"+
"  const TYPE = 11001;\n"+
"  private $req;\n"+
"  \n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt(0); // fixme: status\n"+
"    $out->writeInt(0); // fixme: string: filecontent\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_FileContentExtendedResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StartResponse extends pdb_Message {\n"+
"  const TYPE = 1001;\n"+
"  private $req;\n"+
"  \n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt(0);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StartResponse: \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StartProcessFileNotification extends pdb_Message {\n"+
"  const TYPE = 2009;\n"+
"  public function __construct ($session) {\n"+
"    parent::__construct($session);\n"+
"  }\n"+
"  protected function handleContinueProcessFile($message) {\n"+
"    return true; // next\n"+
"  }\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeString($this->session->currentFrame->filename);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StartProcessFileNotification: {$this->session->currentFrame->filename}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_ContinueProcessFileNotification extends pdb_Message {\n"+
"  const TYPE = 2010;\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_ContinueProcessFileNotification: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_Breakpoint {\n"+
"  public $type, $lifeTime, $file, $line, $condition;\n"+
"  private $id;\n"+
"\n"+
"  public function __construct($type, $lifeTime, $file, $line, $condition, $id) {\n"+
"    $this->type = $type;\n"+
"    $this->lifeTime = $lifeTime;\n"+
"    $this->file = $file;\n"+
"    $this->line = $line;\n"+
"    $this->condition = $condition;\n"+
"    $this->id = $id;\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_Breakpoint: \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_AddBreakpointResponse extends pdb_Message {\n"+
"  const TYPE = 1021;\n"+
"  private $req;\n"+
"  private $id;\n"+
"\n"+
"  private static function getId() {\n"+
"    static $id = 0;\n"+
"    return ++$id;\n"+
"  }\n"+
"\n"+
"  public function __construct($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"    $this->id = self::getId();\n"+
"    $this->session->breakpoints[$this->id] = new pdb_Breakpoint($req->type, $req->lifeTime, $req->file, $req->line, $req->condition, $this->id);\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt(0);\n"+
"    $out->writeInt($this->id);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_AddBreakpointResponse: {$this->id}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_RemoveBreakpointResponse extends pdb_Message {\n"+
"  const TYPE = 1022;\n"+
"  private $req;\n"+
"  private $id;\n"+
"  private $failure;\n"+
"\n"+
"  public function __construct($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"\n"+
"	$this->remove();\n"+
"  }\n"+
"\n"+
"  protected function remove() {\n"+
"	if (isset($this->session->breakpoints[$this->req->bpId])) {\n"+
"	  unset($this->session->breakpoints[$this->req->bpId]);\n"+
"	  $this->failure = 0;\n"+
"	} else {\n"+
"	  $this->failure = -1;\n"+
"	}\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt($this->failure);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_RemoveBreakpointResponse: {$this->id}\";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_RemoveAllBreakpointsResponse extends pdb_RemoveBreakpointResponse {\n"+
"  const TYPE = 1023;\n"+
"  public function __construct($req) {\n"+
"    parent::__construct($req);\n"+
"  }\n"+
"\n"+
"  protected function remove() {\n"+
"	$keys = array_keys($this->session->breakpoints);\n"+
"	foreach($keys as $key)\n"+
"	  unset($this->session->breakpoints[$key]);\n"+
"	\n"+
"	$this->failure = 0;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"\n"+
"  public function __toString () {\n"+
"    return \"pdb_RemoveAllBreakpoinstResponse: {$this->id}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_AddBreakpointRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 21;\n"+
"  public $id;\n"+
"  public $type;\n"+
"  public $lifeTime;\n"+
"\n"+
"  public $file;\n"+
"  public $line;\n"+
"\n"+
"  public $condition;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    $this->type = $in->readShort();\n"+
"    $this->lifeType = $in->readShort();\n"+
"    switch($this->type) {\n"+
"    case 1: \n"+
"      $this->file = $in->readString();\n"+
"      $this->line = $in->readInt();\n"+
"      break;\n"+
"    case 2:\n"+
"      $this->condition = $in->readString();\n"+
"      break;\n"+
"    default: \n"+
"      trigger_error(\"invalid breakpoint\", E_USER_ERROR);\n"+
"    }\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_AddBreakpointResponse ($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    if ($this->type == 1) \n"+
"      return \"pdb_AddBreakpointRequest: {$this->file}, {$this->line}\";\n"+
"    else\n"+
"      return \"pdb_AddBreakpointRequest: {$this->condition}\";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_RemoveAllBreakpointsRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 23;\n"+
"  public $id;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_RemoveAllBreakpointsResponse ($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"	return \"pdb_RemoveAllBreakpointsRequest \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_RemoveBreakpointRequest extends pdb_RemoveAllBreakpointsRequest {\n"+
"  const TYPE = 22;\n"+
"  public $bpId;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"\n"+
"  public function deserialize() {\n"+
"	parent::deserialize();\n"+
"    $in = $this->session->in;\n"+
"    $this->bpId = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_RemoveBreakpointResponse ($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"	return \"pdb_RemoveBreakpointRequest: {$this->bpId}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GetCallStackResponse extends pdb_Message {\n"+
"  const TYPE = 1034;\n"+
"  private $req;\n"+
"\n"+
"  public function __construct($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"\n"+
"	for($frame=$this->session->currentFrame; $frame; $frame=$frame->parent)\n"+
"	  $environments[] = $frame;\n"+
"\n"+
"	$environments = array_reverse($environments);\n"+
"    $n = count($environments);\n"+
"\n"+
"    $out->writeInt($n);\n"+
"    for ($i=0; $i<$n; $i++) {\n"+
"	  $env = $environments[$i];\n"+
"      $out->writeString($env->filename);\n"+
"      $out->writeInt($env->line);\n"+
"      $out->writeInt(0);\n"+
"      $out->writeString($env->filename);\n"+
"      $out->writeInt($env->firstLine);\n"+
"      $out->writeInt(0);\n"+
"      $out->writeInt(0); //fixme: params\n"+
"    }\n"+
"\n"+
"	$out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GetCallStackResponse: \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GetCallStackRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 34;\n"+
"  public $id;\n"+
"	\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_GetCallStackResponse ($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GetCallStackRequest: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GetCWDResponse extends pdb_Message {\n"+
"  const TYPE = 1036;\n"+
"  private $req;\n"+
"\n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt(0);\n"+
"    $out->writeString(getcwd());    \n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GetCWDResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GetCWDRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 36;\n"+
"  public $id;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_GetCWDResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GetCWDRequest: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_MsgEvalResponse extends pdb_Message {\n"+
"  const TYPE = 1031;\n"+
"  private $req;\n"+
"\n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"evalcode:::\".$this->req->code.\"\\n\");\n"+
"	$error = 0;\n"+
"    $code = $this->req->code;\n"+
"    $res = eval(\"return $code ?>\");\n"+
" 	$out->writeInt($error);\n"+
"    $out->writeString($res);\n"+
"\n"+
"	if (PDB_DEBUG) pdb_Logger::debug(\"pdb_MsgEvalResponse: \".print_r($res, true));\n"+
"    $out->flush();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_MsgEvalResponse: \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GetVariableValueResponse extends pdb_Message {\n"+
"  const TYPE = 1032;\n"+
"  private $req;\n"+
"\n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"evalcode:::\".$this->req->code.\"\\n\");\n"+
"	$error = 0;\n"+
"    if ($this->req->code[0]=='$') {\n"+
"\n"+
"	  $this->session->end = true;\n"+
"	  $key = substr($this->req->code, 1);\n"+
"	  if (isset($this->session->currentFrame->vars[$key])) {\n"+
"		$var = $this->session->currentFrame->vars[$key];\n"+
"		$paths = $this->req->paths;\n"+
"		foreach ($paths as $path) {\n"+
"		  if (is_object($var)) {\n"+
"			$var = $var->$path;\n"+
"		  } else {\n"+
"			$var = $var[$path];\n"+
"		  }\n"+
"		}\n"+
"	  } else {\n"+
"		$var = \"${key} not found!\";\n"+
"		$error = -1;\n"+
"	  }\n"+
"	  $ser = $this->session->serializer->serialize($var, $this->req->depth);\n"+
"	  $this->session->end = false;\n"+
"\n"+
"	  $out->writeInt($error);\n"+
"      $out->writeString($ser);\n"+
"	  if (PDB_DEBUG) pdb_Logger::debug(\"pdb_GetVariableValueResponse: \".print_r($var, true).\": ${ser}, error: ${error}\");\n"+
"    } else {\n"+
"	  if (PDB_DEBUG) pdb_Logger::debug(print_r($this->session->currentFrame->vars, true));\n"+
"\n"+
"	  $this->session->end = true;\n"+
"	  $vars = $this->session->currentFrame->vars;\n"+
"	  $ser = $this->session->serializer->serialize($vars, $this->req->depth);\n"+
"	  $this->session->end = false;\n"+
"\n"+
"	  $out->writeInt($error);\n"+
"	  $out->writeString($ser);\n"+
"	  if (PDB_DEBUG) pdb_Logger::debug(\"pdb_GetVariableValueResponse: \".print_r($vars, true).\": ${ser}, error: ${error}\");\n"+
"	}\n"+
"    $out->flush();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GetVariableValueResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_MsgEvalRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 31;\n"+
"  public $id;\n"+
"  public $code;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    $this->code = $in->readString();\n"+
"\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_MsgEvalResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_MsgEvalRequest: {$this->code}\";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GetVariableValueRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 32;\n"+
"  public $id;\n"+
"  public $code;\n"+
"  public $depth;\n"+
"  public $paths;\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    $this->code = $in->readString();\n"+
"    $this->depth = $in->readInt();\n"+
"\n"+
"    $this->paths = array();\n"+
"    $length = $in->readInt();\n"+
"    while($length--) {\n"+
"      $this->paths[] = $in->readString();\n"+
"    }\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_GetVariableValueResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GetVariableValueRequest: {$this->code}, {$this->depth}, paths::\".print_r($this->paths, true);\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StepOverResponse extends pdb_Message {\n"+
"  const TYPE = 1012;\n"+
"  private $req;\n"+
"\n"+
"  public function __construct($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->req->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt(0);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StepOverResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StepOverRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 12;\n"+
"  public $id;\n"+
"  \n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_StepOverResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StepOverRequest: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StepIntoResponse extends pdb_StepOverResponse {\n"+
"  const TYPE = 1011;\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StepIntoResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StepIntoRequest extends pdb_StepOverRequest {\n"+
"  const TYPE = 11;\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_StepIntoResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StepIntoRequest: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StepOutResponse extends pdb_StepOverResponse {\n"+
"  const TYPE = 1013;\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_StepOutResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_StepOutRequest extends pdb_StepOverRequest {\n"+
"  const TYPE = 13;\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_StepOutResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_OutIntoRequest: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GoResponse extends pdb_Message {\n"+
"  const TYPE = 1014;\n"+
"  private $req;\n"+
"\n"+
"  public function __construct ($req) {\n"+
"    parent::__construct($req->session);\n"+
"    $this->req = $req;\n"+
"  }\n"+
"\n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function serialize() {\n"+
"    $out = $this->session->out;\n"+
"    parent::serialize();\n"+
"    $out->writeInt($this->req->id);\n"+
"    $out->writeInt(0);\n"+
"    $out->flush();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GoResponse: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_GoRequest extends pdb_MessageRequest {\n"+
"  const TYPE = 14;\n"+
"  public $id;\n"+
" \n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    $in = $this->session->in;\n"+
"    $this->id = $in->readInt();\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function ack() {\n"+
"    $res = new pdb_GoResponse($this);\n"+
"    $res->serialize();\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_GoRequest: \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_End extends pdb_Message {\n"+
"  const TYPE = 3;\n"+
"  public $id;\n"+
" \n"+
"  public function getType() {\n"+
"    return self::TYPE;\n"+
"  }\n"+
"  public function deserialize() {\n"+
"    if (PDB_DEBUG) pdb_Logger::debug( \"$this\");\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_End: \";\n"+
"  }\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_In {\n"+
"  private $in;\n"+
"  private $len;\n"+
"  private $session;\n"+
"\n"+
"  public function __construct($in, $session) {\n"+
"    $this->in = $in;\n"+
"    $this->len = 0;\n"+
"	$this->session = $session;\n"+
"  }\n"+
"  private function readBytes($n) {\n"+
"    $str = \"\";\n"+
"    while ($n) {\n"+
"      $s = fread($this->in, $n);\n"+
"	  if (feof($this->in)) $this->session->end();\n"+
"\n"+
"      $n -= strlen($s);\n"+
"\n"+
"      $str.=$s;\n"+
"    }\n"+
"    return $str;\n"+
"  }\n"+
"  public function read() {\n"+
"    if(!$this->len) {\n"+
"      $str = $this->readBytes(4);\n"+
"      $lenDesc = unpack(\"N\", $str);\n"+
"      $this->len = array_pop($lenDesc);\n"+
"    }\n"+
"  }\n"+
"  public function readShort() {\n"+
"    $this->read();\n"+
"\n"+
"    $this->len-=2;\n"+
"    $str = $this->readBytes(2);\n"+
"    $lenDesc = unpack(\"n\", $str);\n"+
"    return array_pop($lenDesc);\n"+
"  }\n"+
"  public function readInt() {\n"+
"    $this->read();\n"+
"\n"+
"    $this->len-=4;\n"+
"    $str = $this->readBytes(4);\n"+
"    $lenDesc = unpack(\"N\", $str);\n"+
"    return array_pop($lenDesc);\n"+
"  }\n"+
"  public function readString() {\n"+
"    $this->read();\n"+
"\n"+
"    $length = $this->readInt();\n"+
"    $this->len-=$length;\n"+
"    return $this->readBytes($length);\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_In: \";\n"+
"  }\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"class pdb_Out {\n"+
"  private $out;\n"+
"  private $buf;\n"+
"  private $session;\n"+
"  \n"+
"  public function __construct($out, $session) {\n"+
"    $this->out = $out;\n"+
"    $this->buf = \"\";\n"+
"	$this->session = $session;\n"+
"  }\n"+
"\n"+
"  public function writeShort($val) {\n"+
"    $this->buf.=pack(\"n\", $val);\n"+
"  }\n"+
"  public function writeInt($val) {\n"+
"    $this->buf.=pack(\"N\", $val);\n"+
"  }\n"+
"  public function writeString($str) {\n"+
"    $length = strlen($str);\n"+
"    $this->writeInt($length);\n"+
"    $this->buf.=$str;\n"+
"  }\n"+
"  public function writeUTFString($str) {\n"+
"    $this->writeString(urlencode($str));\n"+
"  }\n"+
"  public function flush() {\n"+
"    $length = strlen($this->buf);\n"+
"    $this->buf = pack(\"N\", $length).$this->buf;\n"+
"    fwrite($this->out, $this->buf);\n"+
"	if (feof($this->out)) $this->session->end();\n"+
"    $this->buf = \"\";\n"+
"  }\n"+
"  public function __toString () {\n"+
"    return \"pdb_Out: \";\n"+
"  }\n"+
"}\n"+
"$pdb_dbg = new pdb_DebugSessionStart(\"&debug_fastfile=1\");\n"+
"pdb_Message::register(new pdb_SetProtocolRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_StartRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_AddFilesRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_FileContentExtendedRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_ContinueProcessFileNotification($pdb_dbg));\n"+
"pdb_Message::register(new pdb_AddBreakpointRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_RemoveBreakpointRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_RemoveAllBreakpointsRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_GetCallStackRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_GetCWDRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_GetVariableValueRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_MsgEvalRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_StepOverRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_StepIntoRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_StepOutRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_GoRequest($pdb_dbg));\n"+
"pdb_Message::register(new pdb_End($pdb_dbg));\n"+
"\n"+
"pdb_Message::register(new pdb_StartProcessFileNotification($pdb_dbg));\n"+
"pdb_Message::register(new pdb_ReadyNotification($pdb_dbg));\n"+
"pdb_Message::register(new pdb_DebugScriptEndedNotification($pdb_dbg));\n"+
"pdb_Message::register(new pdb_HeaderOutputNotification($pdb_dbg));\n"+
"pdb_Message::register(new pdb_OutputNotification($pdb_dbg));\n"+
"pdb_Message::register(new pdb_ErrorNotification($pdb_dbg));\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"function pdb_getDefinedVars($vars1, $vars2) {\n"+
"  //if(isset($vars2)) $vars1['pbd_This'] = $vars2;\n"+
"\n"+
"  unset($vars1['__pdb_Code']);	     // see pdb_Message::doEval()\n"+
"\n"+
"  return $vars1;   \n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"function pdb_startCall($scriptName, $line) {\n"+
"  global $pdb_dbg;\n"+
"\n"+
"  $stepNext = $pdb_dbg->currentFrame->stepNext == pdb_StepIntoRequest::TYPE ? pdb_StepIntoRequest::TYPE : false;\n"+
"\n"+
"  pdb_Logger::debug(\"startCall::$scriptName, $stepNext\");\n"+
"\n"+
"  $env = new pdb_Environment($pdb_dbg->currentFrame, $scriptName, $stepNext, $line);\n"+
"  $pdb_dbg->allFrames[] = $env;\n"+
"\n"+
"  return $env;\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"function pdb_startInclude($scriptName, $once) {\n"+
"  global $pdb_dbg;\n"+
"\n"+
"  $scriptName = $pdb_dbg->resolveIncludePath($scriptName);\n"+
"\n"+
"  // include only from a top-level environment\n"+
"  // initial line# and vars may be wrong due to a side-effect in step\n"+
"  $pdb_dbg->session->currentFrame = $pdb_dbg->session->currentTopLevelFrame;\n"+
"\n"+
"  $stepNext = $pdb_dbg->currentFrame->stepNext == pdb_StepIntoRequest::TYPE ? pdb_StepIntoRequest::TYPE : false;\n"+
"  $pdb_dbg->currentFrame = new pdb_Environment($pdb_dbg->currentFrame, $scriptName, $stepNext, 1);\n"+
"  $pdb_dbg->allFrames[] = $pdb_dbg->currentFrame;\n"+
"\n"+
"  /* BEGIN: StartProcessFileNotification */\n"+
"  $pdb_dbg->getMessageById(pdb_StartProcessFileNotification::TYPE)->handleRequests();\n"+
"  /* ...  set breakpoints ... */\n"+
"  /* END: ContinueProcessFileNotification */\n"+
"\n"+
"  if ($once && isset($pdb_dbg->includedScripts[$scriptName]))\n"+
"	$code = \"<?php ?>\";\n"+
"  else\n"+
"	$code = $pdb_dbg->parseCode(realpath($scriptName), file_get_contents($scriptName));\n"+
"\n"+
"  $pdb_dbg->currentTopLevelFrame = $pdb_dbg->currentFrame;\n"+
"\n"+
"  if (PDB_DEBUG) pdb_Logger::debug(\"include:::$code\");\n"+
"\n"+
"  if ($once) $pdb_dbg->includedScripts[$scriptName] = true;\n"+
"  return $code; // eval -> pdb_step/MSG_READY or pdb_endInclude/MSG_READY OR FINISH\n"+
"}\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"function pdb_endInclude() {\n"+
"  global $pdb_dbg;\n"+
"\n"+
"  $pdb_dbg->currentFrame = $pdb_dbg->currentTopLevelFrame = $pdb_dbg->currentTopLevelFrame->parent;\n"+
"}\n"+
"\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"function pdb_step($filename, $line, $vars) {\n"+
"  global $pdb_dbg;\n"+
"  if ($pdb_dbg->ignoreInterrupt) return;\n"+
"\n"+
"  $pdb_dbg->ignoreInterrupt = true;\n"+
"\n"+
"  // pull the current frame from the stack or the top-level environment\n"+
"  $pdb_dbg->currentFrame = (isset($vars['__pdb_CurrentFrame'])) ? $vars['__pdb_CurrentFrame'] : $pdb_dbg->currentTopLevelFrame;\n"+
"  unset($vars['__pdb_CurrentFrame']);\n"+
"\n"+
"  $pdb_dbg->currentFrame->update($line, $vars);\n"+
"\n"+
"  if ($pdb_dbg->hasBreakpoint($filename, $line)) {\n"+
"	$pdb_dbg->flushOutput();\n"+
"	$stepNext = $pdb_dbg->getMessageById(pdb_ReadyNotification::TYPE)->handleRequests();\n"+
"	pdb_Logger::logDebug(\"continue\");\n"+
"	/* clear all dynamic breakpoints */\n"+
"	foreach ($pdb_dbg->allFrames as $currentFrame)\n"+
"	  $currentFrame->stepNext = false;\n"+
"\n"+
"	/* set new dynamic breakpoint */\n"+
"	if ($stepNext != pdb_GoRequest::TYPE) {\n"+
"	  $currentFrame = $pdb_dbg->currentFrame;\n"+
"\n"+
"	  /* break in current frame or frame below */\n"+
"	  if ($stepNext != pdb_StepOutRequest::TYPE)\n"+
"		$currentFrame->stepNext = $stepNext;\n"+
"\n"+
"	  /* or break in any parent */\n"+
"	  while ($currentFrame = $currentFrame->parent) {\n"+
"		$currentFrame->stepNext = $stepNext;\n"+
"	  }\n"+
"	}\n"+
"  }\n"+
"  $pdb_dbg->ignoreInterrupt = false;\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"function pdb_error_handler($errno, $errstr, $errfile, $errline) {\n"+
"  global $pdb_dbg;\n"+
"  if (PDB_DEBUG) pdb_Logger::debug(\"PHP error $errno: $errstr in $errfile line $errline\");\n"+
"  if ($pdb_dbg->end) return false;\n"+
"\n"+
"  $msg = $pdb_dbg->getMessageById(pdb_ErrorNotification::TYPE);\n"+
"  $msg->setError($errno, $errfile, $errline, $errstr);\n"+
"  $msg->serialize();\n"+
"  return true;\n"+
"}\n"+
"\n"+
"/**\n"+
" * @access private\n"+
" */\n"+
"function pdb_shutdown() {\n"+
"  global $pdb_dbg;\n"+
"  if (PDB_DEBUG) pdb_Logger::debug(\"PHP error: \".print_r(error_get_last(), true));\n"+
"  if ($pdb_dbg->end) return;\n"+
"\n"+
"  $error = error_get_last();\n"+
"  if ($error) {\n"+
"	$msg = $pdb_dbg->getMessageById(pdb_ErrorNotification::TYPE);\n"+
"	$msg->setError($error['type'], $error['file'], $error['line'], $error['message']);\n"+
"	$msg->serialize();\n"+
"  }\n"+
"}\n"+
"\n"+
"\n"+
"function pdb_getDebugHeader($name,$array) {\n"+
"  if (array_key_exists($name,$array)) return $array[$name];\n"+
"  $name=\"HTTP_$name\";\n"+
"  if (array_key_exists($name,$array)) return $array[$name];\n"+
"  return null;\n"+
"}\n"+
"\n"+
"\n"+
"if (!isset($java_include_only) && isset($pdb_script) && $pdb_script!=\"@\") { // not called from JavaProxy.php and pdb_script is set\n"+
"	chdir (dirname ($pdb_script));\n"+
"	$pdb_dbg->handleRequests();\n"+
"}\n"+
"\n"+
"?>\n"+
"";
    public static final byte[] bytes = data.getBytes(); 
}
