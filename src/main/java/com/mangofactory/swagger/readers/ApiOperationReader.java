package com.mangofactory.swagger.readers;

import com.mangofactory.swagger.core.CommandExecutor;
import com.mangofactory.swagger.readers.operation.*;
import com.mangofactory.swagger.scanners.RequestMappingContext;
import com.wordnik.swagger.model.Operation;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static com.mangofactory.swagger.ScalaUtils.*;

public class ApiOperationReader implements Command<RequestMappingContext> {

   private static final Set<RequestMethod> allRequestMethods
         = new HashSet<RequestMethod>(Arrays.asList(RequestMethod.values()));

   @Override
   public void execute(RequestMappingContext outerContext) {

      RequestMappingInfo requestMappingInfo = outerContext.getRequestMappingInfo();
      HandlerMethod handlerMethod = outerContext.getHandlerMethod();

      RequestMethodsRequestCondition requestMethodsRequestCondition = requestMappingInfo.getMethodsCondition();
      List<Operation> operations = newArrayList();

      Set<RequestMethod> requestMethods = requestMethodsRequestCondition.getMethods();
      Set<RequestMethod> supportedMethods = (requestMethods == null || requestMethods.isEmpty())
            ? allRequestMethods : requestMethods;

      for (RequestMethod httpRequestMethod : supportedMethods) {
         CommandExecutor<Map<String, Object>, RequestMappingContext> commandExecutor = new CommandExecutor();

         RequestMappingContext operationRequestMappingContext = new RequestMappingContext(requestMappingInfo, handlerMethod);
         List<Command<RequestMappingContext>> commandList = newArrayList();

         operationRequestMappingContext.put("currentHttpMethod", httpRequestMethod);
         operationRequestMappingContext.put("currentCount", 0);

         commandList.add(new OperationHttpMethodReader());
         commandList.add(new OperationSummaryReader());
         commandList.add(new OperationNotesReader());
         commandList.add(new OperationResponseClassReader()); //td
         commandList.add(new OperationNicknameReader());
         commandList.add(new OperationPositionReader());
         commandList.add(new MediaTypeReader());
         commandList.add(new OperationParameterReader()); //td
         commandList.add(new OperationResponseMessageReader()); //td
         commandList.add(new OperationDeprecatedReader()); //td
         commandExecutor.execute(commandList, operationRequestMappingContext);


         Map<String, Object> operationResultMap = operationRequestMappingContext.getResult();

         List<String> producesMediaTypes = (List<String>) operationResultMap.get("produces");
         List<String> consumesMediaTypes = (List<String>) operationResultMap.get("consumes");
         Operation operation = new Operation(
               (String) operationResultMap.get("httpRequestMethod"),
               (String) operationResultMap.get("summary"),
               (String) operationResultMap.get("notes"),
               "responseClass",
               (String) operationResultMap.get("nickname"),
               (Integer) operationResultMap.get("position"),
               toScalaList(producesMediaTypes),
               toScalaList(consumesMediaTypes),
               emptyScalaList(),
               emptyScalaList(),
               emptyScalaList(),
               emptyScalaList(),
               toOption(operationResultMap.get("deprecated"))
         );

         operations.add(operation);

//                  method: String
//                  summary: String,
//                  notes: String,
//                  responseClass: String,
//                  nickname: String,
//                  position: Int,
//                  produces: List[String] = List.empty,
//                  consumes: List[String] = List.empty,
//                  protocols: List[String] = List.empty,
//                  authorizations: List[String] = List.empty,
//                  parameters: List[Parameter] = List.empty,
//                  responseMessages: List[ResponseMessage] = List.empty,
//            `deprecated`: Option[String] = None)


      }

      outerContext.put("operations", operations);
   }
}
