module org.tsitle.demo_cli_app_critical_path {
	requires com.google.common;
	requires org.jspecify;
	requires io.github.tsitle.criticalpath;
	requires com.google.gson;
	requires info.picocli;

	opens org.tsitle.demo_cli_app_critical_path to info.picocli;
	opens org.tsitle.demo_cli_app_critical_path.json to com.google.gson;
}
