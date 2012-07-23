
loadScripts = (dest, scripts) ->
	loader = (src, handler) ->
		script =  document.createElement("script")
		script.src = src
		script.onload = script.onreadystatechange  = () ->
			script.onreadystatechange = script.onload = null
			if /MSIE ([6-9]+\.\d+);/.test(navigator.userAgent)
				window.setTimeout(() ->
					handler()
				,8,this)
			else
				handler()
		dest.appendChild(script)
	(() ->
		if scripts.length isnt 0
			loader(scripts.shift(), arguments.callee)
	)()