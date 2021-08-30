# 项目说明

把 PDF 文件里面包含的图片提取出来。

# 命令行用法

```
ExtractImage [pdf-file]
```

输出的图片文件保存在 `pdf-file` 同一目录下，文件名格式为 `<base>-<page>-<seq>.<jpg|png>`。

# Java 程序打包成安装包

1. 把 java 程序打包成 executable jar，并复制到 `build/extractimages.jar`。

2. 下载一套 jre 并复制到 `build/jre`。

3. 用 Inno Setup 执行 `build.iss`。

# 参考资料

[Apache PDFBox - A Java PDF Library](https://pdfbox.apache.org/)

[extract images from pdf using pdfbox](https://stackoverflow.com/questions/8705163/extract-images-from-pdf-using-pdfbox)

[ExtractImages.java](https://svn.apache.org/repos/asf/pdfbox/trunk/tools/src/main/java/org/apache/pdfbox/tools/ExtractImages.java)

[How can I convert my Java program to an .exe file?](https://stackoverflow.com/questions/147181/how-can-i-convert-my-java-program-to-an-exe-file)
