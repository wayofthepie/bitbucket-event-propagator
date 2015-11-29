AJS.toInit(function() {
  console.log(AJS.params.baseURL)
  function populateForm() {
    AJS.$.ajax({
      url: AJS.contextPath() + "/rest/event-propagator/1.0/configuration",
      dataType: "json",
      success: function(config) {
        AJS.$("#servers").attr("value", config.servers);
      }
    });
  }
  function updateConfig() {
    AJS.$.ajax({
      url: AJS.contextPath() + "/rest/event-propagator/1.0/configuration",
      type: "PUT",
      contentType: "application/json",
      data: '{ "servers": "' + AJS.$("#servers").attr("value") + '"}',
      processData: false,
      success: function(response, status, xhr) {
        console.log(xhr.status)
      },
      complete: function(xhr, textStatus) {
        console.log(xhr.status);
      }
    });
  }
  populateForm();

  AJS.$("#admin").submit(function(e) {
    e.preventDefault();
    updateConfig();
  });
});