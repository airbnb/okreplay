/*
 * Copyright 2011 Rob Fletcher
 *
 * Converted from Groovy to Java by Sean Freitag
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.tape;

import co.freeside.betamax.message.tape.RecordedRequest;
import co.freeside.betamax.message.tape.RecordedResponse;

import java.util.Date;

public class RecordedInteraction {
    public Date getRecorded() {
        return recorded;
    }

    public void setRecorded(Date recorded) {
        this.recorded = recorded;
    }

    public RecordedRequest getRequest() {
        return request;
    }

    public void setRequest(RecordedRequest request) {
        this.request = request;
    }

    public RecordedResponse getResponse() {
        return response;
    }

    public void setResponse(RecordedResponse response) {
        this.response = response;
    }

    private Date recorded;
    private RecordedRequest request;
    private RecordedResponse response;
}
