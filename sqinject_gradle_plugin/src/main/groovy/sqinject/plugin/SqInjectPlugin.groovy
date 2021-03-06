package sqinject.plugin

import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Plugin
import org.gradle.api.Project

class SqInjectPlugin implements Plugin<Project>{

    @Override
    void apply(Project project) {
        def task = project.tasks.create("SqRGenerator", SqRGenerator.class)
        task.setGroup("sqinject")
        task.outputs.upToDateWhen {false}
        def android = project.extensions.android
        project.plugins.all {
            if (it instanceof LibraryPlugin) {
                android.libraryVariants.all { variant ->
                    def outputDir = project.buildDir.absolutePath + File.separator + "generated/source/sqr/"+variant.dirName
                    variant.outputs.all { output ->
                        task.dependsOn(output.processResources)
                        variant.registerJavaGeneratingTask(task, new File(outputDir))
                    }
                }
            } else if (it instanceof AppPlugin) {
                android.applicationVariants.all { variant ->
                    def outputDir = project.buildDir.absolutePath + File.separator + "generated/source/sqr/"+variant.dirName
                    variant.outputs.all { output ->
                        task.dependsOn(output.processResources)
                        variant.registerJavaGeneratingTask(task, new File(outputDir))
                    }
                }
            }
        }
    }
}