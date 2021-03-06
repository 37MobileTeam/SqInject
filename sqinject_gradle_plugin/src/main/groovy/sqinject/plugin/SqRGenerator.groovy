package sqinject.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.BaseVariant
import org.gradle.api.DefaultTask
import org.gradle.api.DomainObjectSet
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
import java.util.regex.Matcher
import java.util.regex.Pattern

class SqRGenerator extends DefaultTask{

    @TaskAction
    public void generateSqR(){
        def android = project.extensions.android
        project.plugins.all {
            if (it instanceof LibraryPlugin) {
                this.configureR2Generation(project, android.libraryVariants)
            } else if (it instanceof AppPlugin) {
                this.configureR2Generation(project, android.applicationVariants)
            }
        }
    }

    public String getPackageName(BaseVariant variant)  {
        def manifest = variant.sourceSets.get(0).manifestFile
        // According to the documentation, the earlier files in the list are meant to be overridden by the later ones.
        // So the first file in the sourceSets list should be main.
        def result = new XmlSlurper().parse(manifest)
        return result.getProperty("@package").toString()
    }

    public void configureR2Generation(Project project, DomainObjectSet<? extends BaseVariant> variants) {
        variants.all { variant ->
            def rPackage = this.getPackageName(variant)
            println(rPackage)
            rPackage = rPackage.replace(".", "/")
            def variantOutput = variant.outputs.first();

            def processResources
            if(isGradleToolsBuildVersionLow()){
             //适配gradle 2.3.0 -- 3.2.1，缺失processResourcesProvider
                println '当前Android Gradle Plugin Version : ' + getGradleToolsBuildVersion() + '，适配范围:2.3.0 -- 3.2.1'
                processResources = variantOutput.getProcessResources()
            }else{
                processResources = variantOutput.processResourcesProvider.get()
            }

            //R.txt路径
            def rFiles = project.files(processResources.textSymbolOutputFile).builtBy(processResources)
            def RFilePath = rFiles.singleFile.absolutePath
            println("R.txt path: " + RFilePath)

            /**
             * 适配gradle 3.6.0+ ,Cannot get property 'absolutePath' on null object
             * variant.getBuildType().name：debug、release...
             * */
            def buildType = variant.getBuildType().name
            def pickOutputDirPath =  project.buildDir.getAbsolutePath().toString()+ File.separator + "generated"+ File.separator + "not_namespaced_r_class_sources"+ File.separator + buildType+ File.separator + "processDebugResources" + File.separator + "r"
            //R.java路径
            File RClassFile
            def outputDir = processResources.getSourceOutputDir()
            if(outputDir == null){
                RClassFile = new File(pickOutputDirPath + File.separator + rPackage + File.separator + "R.java");
            }else {
                RClassFile = new File(outputDir.absolutePath + File.separator + rPackage + File.separator + "R.java");
            }

            if (!rFiles.singleFile.exists() && !RClassFile.exists() && !(outputDir.exists() && outputDir.name.contains("R.jar"))) {
                println(rFiles.singleFile.absolutePath + "不存在")
                println(RClassFile.absolutePath + "不存在")
                if (outputDir.name.contains("R.jar")) {
                    println(pickOutputDirPath + "不存在")
                }
                return
            }

            //根据R.txt内容，生成SqR.java
            def fileContent
            if(outputDir == null){
                println("pickOutputDirPath: " + pickOutputDirPath)
                fileContent = generateSqRFile(RClassFile,rPackage,RFilePath,pickOutputDirPath)
            }else {
                println("processResources output Dir: " + outputDir.absolutePath)
                fileContent = generateSqRFile(RClassFile,rPackage,RFilePath,outputDir.absolutePath)
            }

            def newClassName = "SqR"
            def newClassPath = project.buildDir.absolutePath + File.separator + "generated/source/sqr/" + variant.dirName + "/"+rPackage + "/SqR.java"
            println "输出文件：" + newClassPath
            this.write(newClassPath, fileContent)
            println "插件自动生成SqR文件成功！"
        }
    }

    /**
     * 参数解析：
     * RClassFile：R.java
     * rPackage：当前的包名
     * RFilePath：R.txt 路径
     * outputDirPath：processResources的输出路径（gradle3.6.0+，需要拼接）
     * */
    public String generateSqRFile(File RClassFile, String rPackage, String RFilePath,String outputDirPath){

        def rFileContent = "";
        if (RClassFile.exists()) {
            println("use outputDir generate SqR")
            File tempFile = new File(outputDirPath + File.separator + rPackage + File.separator + "R.java");
            println("R file path: " + tempFile.absolutePath);
            rFileContent = tempFile.text
            Pattern pattern = Pattern.compile("public static(.*?) int (.*?)=(.*?);");
            Matcher matcher = pattern.matcher(rFileContent);
            // 将原先的R文件的int换成String，并将其值使用变量名赋值
            while (matcher.find()){
                String replace = "public static final String " + matcher.group(2) + " = \"" + matcher.group(2) + "\";";
                rFileContent = rFileContent.replaceAll(matcher.group(), replace);
            }
            rFileContent = rFileContent.replaceAll("class R", "class SqR")
        }
        //由于classloader加载类机制问题，暂时不用
//            else if (outputDir.exists() && outputDir.name.contains("R.jar")) {
//                println("use outputDir R.jar generate SqR")
//                rFileContent = SqRBuilderByJar.build(outputDir.absolutePath, this.getPackageName(variant))
//            }
        else {
            println("use R.txt generate SqR")
            rFileContent = "package " + rPackage.replace("/", ".") + ";\n" + "public final class SqR {" + "\n";
            try {
                Map<String, List<String>> map = new HashMap<>();
                FileInputStream fileInputStream = new FileInputStream(RFilePath);
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))
                String line = null
                while((line = bufferedReader.readLine()) != null) {
                    if (line != null) {
                        String[] words = line.split(" ");
                        if (words.length > 2) {
                            String type = words[1];
                            String field = words[2];
                            if (map.get(type) == null) {
                                List<String> list = new ArrayList<>();
                                list.add(field);
                                map.put(type, list)
                            } else {
                                map.get(type).add(field)
                            }
                        }
                    }
                }
                if (map != null) {
                    for (Map.Entry<String, List<String>> entry : map.entrySet()) {
                        String innerClass = "    public static class " + entry.getKey() + "{\n";
                        for (String field : entry.getValue()) {
                            innerClass += "        public static final String " + field + " = " + "\"" + field + "\";\n";
                        }
                        innerClass += "    }\n";
                        rFileContent += innerClass;
                    }
                }
                rFileContent += "}";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return rFileContent
    }

    void write(String filePath, String content) {
        File file = new File(filePath)
        if (!file.exists()){
            file.getParentFile().mkdirs()
            file.createNewFile()
        }
        BufferedWriter bw = null;

        try {
            // 根据文件路径创建缓冲输出流
            bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
            // 将内容写入文件中
            bw.write(content);
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭流
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    bw = null;
                }
            }
        }
    }

    //读取根目录下的gradle插件版本
    String getGradleToolsBuildVersion(){
        String path = this.project.rootDir.absolutePath
        String tarStr = "com.android.tools.build"
        String result = null;
            try{
                File file = new File(path + File.separator + "build.gradle")
                if(!file.exists()){
                    println 'Gradle File path error,check!'
                    return null
                }
                file.withReader { reader ->
                    def lines = reader.readLines()
                    lines.each { String eachLine ->
                        String thisLine = eachLine.trim()
                        if(thisLine.startsWith("classpath") && thisLine.contains(tarStr)){
                                String[] target = thisLine.split(":")
                                result = target[target.size()-1].replace("'","")
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace()
            }
        return result;
    }

    boolean isGradleToolsBuildVersionLow() {
        String targetVersion = "3.2.1"
        boolean equalValue = false
        String[] versions = getGradleToolsBuildVersion().split("\\.")
        String[] targets = targetVersion.split("\\.")
        int size = versions.size() < targets.size() ? versions.size() : targets.size()

        for(int index = 0 ; index < size ; index++){
            if(targets[index] > versions[index]){
                return true
            }else if(targets[index] < versions[index]){
                return false
            }else {
                equalValue = true
            }
        }
        return equalValue
    }
}