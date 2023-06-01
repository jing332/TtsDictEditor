package com.github.jing332

object Constants {
    val DICT_TXT by lazy {
        """
            #注释以#开头
            #普通替换规则:source=target
            飞凫="飞凫"
            曾照=céng照
            #正则替换规则:r:"^regex${'$'}"=target
            r:"^(?<=装)[xX]${'$'}"=逼
        """.trimIndent()
    }
    val DICT_TXT_ADVANCED by lazy {
        """
            
        """.trimIndent()
    }
    val TTSRV_REPLACES_JSON by lazy {
        """
            [{
            		"group": {
            			"id": 1,
            			"name": "默认分组"
            		},
            		"list": [{
            			"id": 207,
            			"name": "李朝歌➡️李zhāo歌",
            			"pattern": "李朝歌",
            			"replacement": "李钊歌",
            			"order": 6
            		}]
            	},
            	{
            		"group": {
            			"id": 1676833236984,
            			"name": "字词_正则",
            			"order": 1
            		},
            		"list": [{
            				"id": 101,
            				"groupId": 1676833236984,
            				"name": "㊣ 长➡️zhǎng",
            				"isEnabled": false,
            				"isRegex": true,
            				"pattern": "(?<!头发)长(?=公主|见识|[孙老者相房势史幼官膘进子兄])|(?<=董事|[生学成家连道师队族首市科州助兄厂机屯村])长(?!头发|乐公|[期久])",
            				"replacement": "掌",
            				"order": 9
            			},
            			{
            				"id": 102,
            				"groupId": 1676833236984,
            				"name": "㊣ 参➡️shēn",
            				"isEnabled": false,
            				"isRegex": true,
            				"pattern": "(?<=(高丽|百济|新罗|西洋|[山老卖的党海白杏沙丹人]))(参)(?!考)|(参)(?=[商茶](?!考))",
            				"replacement": "身",
            				"order": 5
            			}
            		]
            	}
            ]
        """.trimIndent()
    }
}