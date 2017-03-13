<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:param name="ParamName"/>

    <xsl:template match="/*[name()='Payload']">
        <html>

            <head>
                <title>Project</title>
                <style type="text/css">
                    table {
                    font-family: verdana;
                    }

                    tr {
                    height: 30px;
                    }

                    td {
                    width: 200px;
                    border: solid 1px grey
                    }

                    tr.title {
                    background: #efe7d9 ;
                    font: 16 verdana;
                    padding: 0 0 0 15px;
                    }
                </style>
            </head>

            <body>
                <h1><xsl:value-of select="$ParamName"/></h1>
                <table>
                    <tr class="title">
                        <td>Name</td>
                        <td>Status</td>
                        <td>Id</td>
                    </tr>
                    <xsl:for-each select="./*[name()='Projects']/*[name()='Project']">
                        <xsl:if test="./*[name()='projectName'] = $ParamName">
                            <xsl:for-each select="./*[name()='groups']/*[name()='Group']">
                                <tr>
                                    <td><xsl:value-of select="./*[name()='name']"/></td>
                                    <td><xsl:value-of select="./@value"/></td>
                                    <td><xsl:value-of select="./@id"/></td>
                                </tr>
                            </xsl:for-each>
                        </xsl:if>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>