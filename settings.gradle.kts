rootProject.name = "Proyecto_Avanzada"
include("src:test:java")
findProject(":src:test:java")?.name = "java"
