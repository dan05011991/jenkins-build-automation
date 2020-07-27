import org.boon.Boon;

def jsonEditorOptions = Boon.fromJson(/{
        disable_edit_json: true,
        disable_properties: true,
        no_additional_properties: true,
        disable_collapse: true,
        disable_array_add: true,
        disable_array_delete: true,
        disable_array_reorder: true,
        theme: "bootstrap3",
        iconlib:"fontawesome4",
       "schema":{
  "title": "Applications",
  "type": "array",
  "format":"tabs",
  "items": {
	 "title": "Application",
	 "headerTemplate": "{{self.appName}}",
	 "type": "object",
	 "properties": {
		"appName" : {
			 "title": "Application Name",
			 "type": "string",
			 "readOnly": "false"
		 },
		"b_confirm" : {
			 "title": "Confirm Deployment",
			 "type": "checkbox"

		 },
		 "configurations": {
			 "title": "Configurations",
			 "type": "array",
			 "format":"tabs",
			 "items": {
				 "title": "Configuration",
				 "headerTemplate": "{{self.a_name}}",
				 "type": "object",
				 "properties": {
					"a_name" : {
						 "title": "Configuration",
						 "type": "string",
						 "readOnly": "true"

					 },
					 "b_overrides": {
						 "title": "Properties",
						 "type": "array",
						 "format": "table",
						 "items": {
							 "type": "object",
							  "properties": {
								  "name" : {
									  "type": "string",
									  "readOnly": "true"
								  },
								  "value" : {
									  "type": "string"
								  }
							  }
						 }
					 },
					 "environments": {
						 "title": "Environments",
						 "type": "array",
						 "format":"tabs",
						 "items": {
							 "title": "Server",
							 "headerTemplate": "{{self.name}}",
							 "type": "object",

							 "properties": {
								"name" : {
									 "title": "Server",
									 "type": "string",
									 "readOnly": "true"
								 },
								 "VMs": {
									 "type": "array",
									 "format": "table",
									 "items": {
										 "type": "object",
										  "properties": {
											  "Owner" : {
												  "type": "string",
												  "readOnly": "true"
											  },
											  "Deploy" : {
												  "type": "checkbox"
											  }
										  }
									 }
								 }
							 }
						 }
					 }
				 }
			 }
		 }
	 }
  }
},
startval: [
  {
    "appName": "SF Core",
	"b_confirm": false,
    "configurations": [
      {
        "a_name": "djfoley",
		"b_overrides": [
		  {
			"name": "keystore_mount",
			"value": "dev1_url"
		  },
		  {
			"name": "port_mappings",
			"value": "dev1_password"
		  },
		  {
			"name": "extra_hosts",
			"value": "dev1_password"
		  },
		  {
			"name": "container_name",
			"value": "dev1_password"
		  }
		],
        "environments": [
			{
				"name": "Dev",
				"VMs": [
				  {
					"Owner": "Dan F",
					"Deploy": false
				  },
				  {
					"Owner": "Bill V",
					"Deploy": false
				  }
				]
			},
			{
				"name": "Reg",
				"VMs": [
				  {
					"Owner": "Dan F",
					"Deploy": false
				  },
				  {
					"Owner": "Bill V",
					"Deploy": false
				  }
				]
			},
			{
				"name": "Int",
				"VMs": [
				  {
					"Owner": "Dan F",
					"Deploy": false
				  },
				  {
					"Owner": "Bill V",
					"Deploy": false
				  }
				]
			},
			{
				"name": "Ref",
				"VMs": [
				  {
					"Owner": "Dan F",
					"Deploy": false
				  },
				  {
					"Owner": "Bill V",
					"Deploy": false
				  }
				]
			},
			{
				"name": "Ops",
				"VMs": [
				  {
					"Owner": "Dan F",
					"Deploy": false
				  },
				  {
					"Owner": "Bill V",
					"Deploy": false
				  }
				]
			}
		]
      },
	  {
        "a_name": "devprod",
		"b_overrides": [
		  {
			"name": "database_url",
			"value": "devprod_url"
		  },
		  {
			"name": "database_password",
			"value": "devprod_password"
		  }
		],
        "environments": [
			{
				"name": "agt11",
				"properties": [
				  {
					"name": "database_url",
					"value": "agt11_prod_url",
					"override": "true"
				  },
				  {
					"name": "database_password",
					"value": "agt11_prod_password",
					"override": "true"
				  }
				]
			},
			{
				"name": "agt12",
				"properties": [
				  {
					"name": "database_url",
					"value": "agt12_prod_url",
					"override": "true"
				  },
				  {
					"name": "database_password",
					"value": "agt12_prod_password",
					"override": "true"
				  }
				]
			}
		]
      }
    ]
  },
  {
    "appName": "SF UI",
	"b_confirm": false,
    "configurations": [
      {
        "a_name": "dev1",
		"b_overrides": [
		  {
			"name": "database_url",
			"value": "dev1_url"
		  },
		  {
			"name": "database_password",
			"value": "dev1_password"
		  }
		],
        "environments": [
			{
				"name": "app11",
				"properties": [
				  {
					"name": "database_url",
					"value": "app11_url",
					"override": "true"
				  },
				  {
					"name": "database_password",
					"value": "app11_password",
					"override": "true"
				  }
				]
			},
			{
				"name": "app12",
				"properties": [
				  {
					"name": "database_url",
					"value": "app12_url",
					"override": "true"
				  },
				  {
					"name": "database_password",
					"value": "app12_password",
					"override": "true"
				  }
				]
			}
		]
      },
	  {
        "a_name": "devprod",
		"b_overrides": [
		  {
			"name": "database_url",
			"value": "devprod_url"
		  },
		  {
			"name": "database_password",
			"value": "devprod_password"
		  }
		],
        "environments": [
			{
				"name": "app11",
				"properties": [
				  {
					"name": "database_url",
					"value": "agt11_prod_url",
					"override": "true"
				  },
				  {
					"name": "database_password",
					"value": "app11_prod_password",
					"override": "true"
				  }
				]
			},
			{
				"name": "agt12",
				"properties": [
				  {
					"name": "database_url",
					"value": "app12_prod_url",
					"override": "true"
				  },
				  {
					"name": "database_password",
					"value": "app12_prod_password",
					"override": "true"
				  }
				]
			}
		]
      }
    ]
  }
]
}

}/);

return jsonEditorOptions;