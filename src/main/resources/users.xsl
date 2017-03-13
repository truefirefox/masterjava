<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

    <xsl:template match="/*[name()='Payload']">
        <html>

            <head>
                <title>Users</title>
                <style type="text/css">
                    table {
                        font-family: verdana;
                    }

                    tr {
                        height: 30px;
                    }

                    td {
                        border: solid 1px grey
                    }

                    td.col1 {
                        width: 100px;
                    }

                    td.col2 {
                        width: 200px;
                    }

                    tr.title {
                        background: #efe7d9 ;
                        font: 16 verdana;
                        padding: 0 0 0 15px;
                    }
                </style>
            </head>

            <body>
                <table>
                    <tr class="title">
                        <td>Name</td>
                        <td>Groups</td>
                    </tr>
                    <xsl:for-each select="./*[name()='Users']/*[name()='User']">
                        <tr>
                            <td class="col1"><xsl:value-of select="./*[name()='fullName']"/></td>
                            <td class="col2"><xsl:value-of select="./@groups"/></td>
                        </tr>
                    </xsl:for-each>
                </table>
            </body>
        </html>
    </xsl:template>
</xsl:stylesheet>